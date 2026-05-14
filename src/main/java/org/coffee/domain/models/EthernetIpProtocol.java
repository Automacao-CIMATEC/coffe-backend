package org.coffee.domain.models;

import etherip.EtherNetIP;
import etherip.types.CIPData;
import org.coffee.domain.abstracts.AbstractProtocol;

/**
 * Implementacao do protocolo Ethernet/IP para comunicacao com CLPs Rockwell.
 *
 * Suporta exclusivamente os seguintes modelos:
 *   - CompactLogix
 *   - ControlLogix
 *   - MicroLogix
 *
 * A comunicacao e realizada via biblioteca etherip (ornl-epics/etherip),
 * que implementa o protocolo CIP sobre TCP (porta 44818).
 *
 * Nota sobre endereçamento de tags:
 *   Suporta tags simbolicas ("MinhaTag"), membros de estrutura ("Estrutura.Membro"),
 *   arrays ("MinhaTag[0]") e endereçamento de bit via ponto ("MinhaTag.3").
 *   Para bits, realiza leitura-modificacao-escrita sobre o inteiro pai.
 */
public class EthernetIpProtocol extends AbstractProtocol {

    // Porta padrao do protocolo Ethernet/IP (CIP sobre TCP)
    public static final int ETHERNET_IP_PORT = 44818;

    // Slot do controlador no chassi (normalmente 0 para CompactLogix/ControlLogix)
    private final int slot;

    // Sessao ativa com o CLP — gerenciada por openConnection/closeConnection
    private EtherNetIP plc;

    // Flag de estado de conexao
    private boolean isConnected = false;

    /**
     * Construtor principal.
     *
     * @param ip   endereco IP do CLP
     * @param slot slot do controlador no chassi (normalmente 0)
     */
    public EthernetIpProtocol(String ip, int slot) {
        super(ip, ETHERNET_IP_PORT);
        this.slot = slot;
    }

    /**
     * Construtor de conveniencia para slot 0 (caso mais comum).
     *
     * @param ip endereco IP do CLP
     */
    public EthernetIpProtocol(String ip) {
        this(ip, 0);
    }

    // =========================================================================
    // GERENCIAMENTO DE CONEXAO
    // =========================================================================

    /**
     * Abre a conexao TCP com o CLP e registra a sessao Ethernet/IP.
     * Implementa AbstractProtocol.openConnection().
     */
    @Override
    public void openConnection() {
        try {
            // Cria nova instancia de sessao a cada abertura de conexao
            plc = new EtherNetIP(ip, slot);
            // connectTcp() abre o socket TCP na porta 44818 e registra a sessao CIP
            plc.connectTcp();
            isConnected = true;
            System.out.println("==> Ethernet/IP: conectado ao CLP " + ip + " (slot " + slot + ")");
        } catch (Exception e) {
            isConnected = false;
            System.err.println("==> Ethernet/IP: falha na conexao: " + e.getMessage());
            throw new RuntimeException("Falha ao conectar ao CLP " + ip + ": " + e.getMessage(), e);
        }
    }

    /**
     * Fecha a sessao Ethernet/IP e o socket TCP subjacente.
     * Implementa AbstractProtocol.closeConnection().
     */
    @Override
    public void closeConnection() {
        if (plc != null) {
            try {
                // close() encerra a sessao CIP e fecha o socket TCP
                plc.close();
                System.out.println("==> Ethernet/IP: conexao encerrada com " + ip);
            } catch (Exception e) {
                System.err.println("==> Ethernet/IP: erro ao encerrar conexao: " + e.getMessage());
            } finally {
                isConnected = false;
                plc = null;
            }
        }
    }

    // =========================================================================
    // METODOS DE LEITURA — implementacoes de AbstractProtocol
    // =========================================================================

    /**
     * Le uma tag inteira (SINT, INT ou DINT) do CLP.
     * O parametro 'parameter' nao e utilizado no protocolo Ethernet/IP
     * pois o endereçamento e por nome simbolico, nao por endereco numerico.
     * Use readInt(String tagName) para especificar a tag.
     */
    @Override
    public int readDataInt(int parameter) {
        throw new UnsupportedOperationException(
            "Use readInt(String tagName) para Ethernet/IP. " +
            "O endereçamento e por nome simbolico, nao por endereco numerico."
        );
    }

    /**
     * Le uma tag de ponto flutuante (REAL) do CLP.
     */
    @Override
    public float readDataFloat(int parameter) {
        throw new UnsupportedOperationException(
            "Use readReal(String tagName) para Ethernet/IP."
        );
    }

    /**
     * Le uma tag booleana do CLP.
     */
    @Override
    public boolean readDataBoolean(int param) {
        throw new UnsupportedOperationException(
            "Use readBool(String tagName) para Ethernet/IP."
        );
    }

    /**
     * Nao aplicavel ao protocolo Ethernet/IP sem nome de tag.
     */
    @Override
    public String readDataString() {
        throw new UnsupportedOperationException(
            "Use readString(String tagName) para Ethernet/IP."
        );
    }

    /**
     * Nao aplicavel ao protocolo Ethernet/IP sem nome de tag.
     */
    @Override
    public String readDataString(int startingAddress, int registerCount) {
        throw new UnsupportedOperationException(
            "Use readString(String tagName) para Ethernet/IP."
        );
    }

    // =========================================================================
    // METODOS DE ESCRITA — implementacoes de AbstractProtocol (sem tag name)
    // =========================================================================

    @Override public void writeData(int data) { throw new UnsupportedOperationException("Use writeInt(String tagName, int value)."); }
    @Override public void writeData(float data) { throw new UnsupportedOperationException("Use writeReal(String tagName, float value)."); }
    @Override public void writeData(boolean data) { throw new UnsupportedOperationException("Use writeBool(String tagName, boolean value)."); }
    @Override public void writeData(String data) { throw new UnsupportedOperationException("Use writeString(String tagName, String value)."); }
    @Override public void writeData(int address, int data) { throw new UnsupportedOperationException("Use writeInt(String tagName, int value)."); }
    @Override public void writeData(int address, float data) { throw new UnsupportedOperationException("Use writeReal(String tagName, float value)."); }
    @Override public void writeData(int address, boolean data) { throw new UnsupportedOperationException("Use writeBool(String tagName, boolean value)."); }
    @Override public void writeData(int startingAddress, String data) { throw new UnsupportedOperationException("Use writeString(String tagName, String value)."); }

    // =========================================================================
    // API PUBLICA — leitura e escrita por nome simbolico de tag
    // =========================================================================

    /**
     * Le uma tag booleana do CLP.
     *
     * Suporta endereçamento de bit via notacao de ponto (ex: "Tag.0", "Estrutura.Membro.3").
     * Nesse caso, le o inteiro pai e extrai o bit especificado via operacao de mascara.
     *
     * @param tagName nome simbolico da tag, com ou sem sufixo de bit
     * @return valor booleano da tag ou do bit especificado
     * @throws Exception se a leitura falhar ou a tag nao existir
     */
    public boolean readBool(String tagName) throws Exception {
        verificarConexao();

        // Verifica se e um endereçamento de bit (ex: "Tag.0")
        BitAddress bit = BitAddress.parse(tagName);
        if (bit != null) {
            // Le o inteiro pai e extrai o bit pelo indice via deslocamento
            CIPData data = plc.readTag(bit.parentTag);
            int intValue = data.getNumber(0).intValue();
            return ((intValue >> bit.bitIndex) & 1) == 1;
        }

        CIPData data = plc.readTag(tagName);
        // Valor 0 = false, qualquer outro valor = true
        return data.getNumber(0).intValue() != 0;
    }

    /**
     * Le uma tag inteira (SINT, INT ou DINT) do CLP.
     *
     * @param tagName nome simbolico da tag no CLP
     * @return valor inteiro da tag
     * @throws Exception se a leitura falhar ou a tag nao existir
     */
    public int readInt(String tagName) throws Exception {
        verificarConexao();
        CIPData data = plc.readTag(tagName);
        return data.getNumber(0).intValue();
    }

    /**
     * Le uma tag de ponto flutuante (REAL) do CLP.
     *
     * @param tagName nome simbolico da tag no CLP
     * @return valor float da tag
     * @throws Exception se a leitura falhar ou a tag nao existir
     */
    public float readReal(String tagName) throws Exception {
        verificarConexao();
        CIPData data = plc.readTag(tagName);
        return data.getNumber(0).floatValue();
    }

    /**
     * Le uma tag de string (STRUCT_STRING) do CLP.
     *
     * @param tagName nome simbolico da tag no CLP
     * @return valor string da tag
     * @throws Exception se a leitura falhar ou a tag nao existir
     */
    public String readString(String tagName) throws Exception {
        verificarConexao();
        // readStringTags() usa o servico CIP correto para tags do tipo STRING do Rockwell
        CIPData[] results = plc.readStringTags(tagName);
        if (results == null || results.length == 0 || results[0] == null) {
            throw new Exception("Leitura de STRING retornou resultado nulo para tag: " + tagName);
        }
        return results[0].getString();
    }

    /**
     * Escreve um valor booleano em uma tag do CLP.
     *
     * Suporta endereçamento de bit via notacao de ponto (ex: "Tag.0").
     * Para bits, executa leitura-modificacao-escrita sobre o inteiro pai.
     *
     * ATENCAO: a operacao de bit NAO e atomica. Em sistemas com escrita
     * concorrente no mesmo DINT, utilize tags BOOL dedicadas no CLP.
     *
     * @param tagName nome simbolico da tag, com ou sem sufixo de bit
     * @param value   valor booleano a ser escrito
     * @throws Exception se a leitura ou escrita falhar
     */
    public void writeBool(String tagName, boolean value) throws Exception {
        verificarConexao();

        // Verifica se e um endereçamento de bit (ex: "Tag.0")
        BitAddress bit = BitAddress.parse(tagName);
        if (bit != null) {
            // Leitura do inteiro pai para obter o valor atual completo
            CIPData data = plc.readTag(bit.parentTag);
            int currentValue = data.getNumber(0).intValue();

            int newValue;
            if (value) {
                // SET: ativa o bit via OR com mascara (ex: bit 2 -> OR 0b00000100)
                newValue = currentValue | (1 << bit.bitIndex);
            } else {
                // CLEAR: desativa o bit via AND com mascara invertida (ex: bit 2 -> AND 0b11111011)
                newValue = currentValue & ~(1 << bit.bitIndex);
            }

            // Escreve o inteiro modificado de volta na tag pai
            data.set(0, (long) newValue);
            plc.writeTag(bit.parentTag, data);
            return;
        }

        // Escrita direta para tags BOOL nativas (sem notacao de bit)
        CIPData data = plc.readTag(tagName);
        // set(index, Number) - altera o valor sem alterar o tipo CIP registrado no CLP
        data.set(0, value ? 1L : 0L);
        plc.writeTag(tagName, data);
    }

    /**
     * Escreve um valor inteiro em uma tag do CLP (SINT, INT ou DINT).
     *
     * @param tagName nome simbolico da tag no CLP
     * @param value   valor a ser escrito
     * @throws Exception se a escrita falhar
     */
    public void writeInt(String tagName, int value) throws Exception {
        verificarConexao();
        // Leitura previa para preservar o tipo CIP exato (SINT, INT ou DINT)
        CIPData data = plc.readTag(tagName);
        data.set(0, (long) value);
        plc.writeTag(tagName, data);
    }

    /**
     * Escreve um valor de ponto flutuante em uma tag REAL do CLP.
     *
     * @param tagName nome simbolico da tag no CLP
     * @param value   valor a ser escrito
     * @throws Exception se a escrita falhar
     */
    public void writeReal(String tagName, float value) throws Exception {
        verificarConexao();
        // Leitura previa para preservar o tipo CIP (REAL)
        CIPData data = plc.readTag(tagName);
        data.set(0, (double) value);
        plc.writeTag(tagName, data);
    }

    /**
     * Escreve um valor string em uma tag STRING do CLP.
     *
     * @param tagName nome simbolico da tag no CLP
     * @param value   valor a ser escrito
     * @throws Exception se a escrita falhar
     */
    public void writeString(String tagName, String value) throws Exception {
        verificarConexao();
        // Leitura previa para obter o CIPData com tipo STRUCT_STRING correto
        CIPData[] results = plc.readStringTags(tagName);
        if (results == null || results.length == 0 || results[0] == null) {
            throw new Exception("Nao foi possivel ler a tag STRING para escrita: " + tagName);
        }
        CIPData data = results[0];
        // setString() usa o encoding especifico do Rockwell para tags STRING
        data.setString(value);
        plc.writeTag(tagName, data);
    }

    // =========================================================================
    // UTILITARIOS
    // =========================================================================

    /**
     * Verifica se a conexao esta ativa antes de realizar qualquer operacao.
     *
     * @throws IllegalStateException se a conexao nao estiver estabelecida
     */
    private void verificarConexao() {
        if (!isConnected || plc == null) {
            throw new IllegalStateException(
                "Conexao Ethernet/IP nao estabelecida. Chame openConnection() antes de operar."
            );
        }
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    public int getSlot() {
        return slot;
    }

    // =========================================================================
    // CLASSE AUXILIAR PARA ENDEREÇAMENTO DE BIT
    // =========================================================================

    /**
     * Representa o resultado do parse de uma tag com endereçamento de bit.
     *
     * Exemplos:
     *   "MinhaTag.0"              -> parentTag="MinhaTag",            bitIndex=0
     *   "Estrutura.Membro.3"      -> parentTag="Estrutura.Membro",    bitIndex=3
     *   "MinhaTag"                -> null (sem bit address)
     *   "Estrutura.Membro"        -> null (membro de estrutura, nao bit)
     */
    private static class BitAddress {

        // Tag pai sem o sufixo de bit
        final String parentTag;

        // Indice do bit dentro do inteiro (0 a 31 para DINT)
        final int bitIndex;

        private BitAddress(String parentTag, int bitIndex) {
            this.parentTag = parentTag;
            this.bitIndex = bitIndex;
        }

        /**
         * Tenta fazer o parse de uma tag com notacao de bit.
         *
         * O criterio e que o ultimo segmento apos o ultimo ponto seja um numero
         * inteiro entre 0 e 31. Segmentos nao numericos sao membros de estrutura.
         *
         * @param tagName nome completo da tag
         * @return BitAddress se o ultimo segmento for indice de bit, null caso contrario
         */
        static BitAddress parse(String tagName) {
            if (tagName == null || !tagName.contains(".")) {
                return null;
            }

            int lastDot = tagName.lastIndexOf('.');
            String lastSegment = tagName.substring(lastDot + 1);

            try {
                int bitIndex = Integer.parseInt(lastSegment);

                if (bitIndex < 0 || bitIndex > 31) {
                    throw new IllegalArgumentException(
                        "Indice de bit fora do range valido (0-31): " + bitIndex
                    );
                }

                String parentTag = tagName.substring(0, lastDot);
                return new BitAddress(parentTag, bitIndex);

            } catch (NumberFormatException e) {
                // Ultimo segmento nao e numero — e membro de estrutura, nao bit
                return null;
            }
        }
    }
}
