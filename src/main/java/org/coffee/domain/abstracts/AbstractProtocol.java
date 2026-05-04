package org.coffee.domain.abstracts;

/**
 * Classe abstrata base para todos os protocolos de comunicação.
 * Define a interface comum que todas as implementações de protocolo devem seguir.
 */
public abstract class AbstractProtocol {

    /** Endereço IP da instância do protocolo */
    protected String ip;

    /** Porta IP da instância do protocolo */
    protected int port;

    /**
     * Construtor da classe abstrata de protocolo.
     *
     * @param ip Endereço IP do servidor/dispositivo
     * @param port Porta de comunicação
     */
    public AbstractProtocol(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    /**
     * Abre a conexão entre cliente e servidor.
     * Implementações devem estabelecer a conexão de rede apropriada.
     */
    public abstract void openConnection();

    /**
     * Fecha a conexão entre cliente e servidor.
     * Implementações devem liberar todos os recursos de rede utilizados.
     */
    public abstract void closeConnection();

    /**
     * Lê um valor inteiro do servidor/dispositivo.
     *
     * @param parameter Parâmetro ou endereço de leitura
     * @return Valor inteiro lido
     */
    public abstract int readDataInt(int parameter);

    /**
     * Lê um valor ponto flutuante do servidor/dispositivo.
     *
     * @param parameter Parâmetro ou endereço de leitura
     * @return Valor float lido
     */
    public abstract float readDataFloat(int parameter);

    /**
     * Lê um valor booleano do servidor/dispositivo.
     *
     * @param param Parâmetro ou endereço de leitura
     * @return Valor booleano lido
     */
    public abstract boolean readDataBoolean(int param);

    /**
     * Lê uma string do servidor/dispositivo.
     *
     * @return String lida
     */
    public abstract String readDataString();

    /**
     * Lê uma string do servidor/dispositivo a partir de um endereço específico.
     *
     * @param startingAddress Endereço inicial de leitura
     * @param registerCount Quantidade de registradores a ler
     * @return String lida
     */
    public abstract String readDataString(int startingAddress, int registerCount);

    /**
     * Escreve um valor inteiro no servidor/dispositivo.
     *
     * @param data Valor inteiro a ser escrito
     */
    public abstract void writeData(int data);

    /**
     * Escreve um valor inteiro em um endereço específico.
     *
     * @param address Endereço de escrita
     * @param data Valor inteiro a ser escrito
     */
    public abstract void writeData(int address, int data);

    /**
     * Escreve um valor ponto flutuante no servidor/dispositivo.
     *
     * @param data Valor float a ser escrito
     */
    public abstract void writeData(float data);

    /**
     * Escreve um valor ponto flutuante em um endereço específico.
     *
     * @param address Endereço de escrita
     * @param data Valor float a ser escrito
     */
    public abstract void writeData(int address, float data);

    /**
     * Escreve um valor booleano no servidor/dispositivo.
     *
     * @param data Valor booleano a ser escrito
     */
    public abstract void writeData(boolean data);

    /**
     * Escreve um valor booleano em um endereço específico.
     *
     * @param address Endereço de escrita
     * @param data Valor booleano a ser escrito
     */
    public abstract void writeData(int address, boolean data);

    /**
     * Escreve uma string no servidor/dispositivo.
     *
     * @param data String a ser escrita
     */
    public abstract void writeData(String data);

    /**
     * Escreve uma string em um endereço específico.
     *
     * @param startingAddress Endereço inicial de escrita
     * @param data String a ser escrita
     */
    public abstract void writeData(int startingAddress, String data);

    /**
     * Verifica se a conexão está ativa.
     *
     * @return true se conectado, false caso contrário
     */
    public abstract boolean isConnected();

    /**
     * Retorna o endereço IP configurado.
     *
     * @return Endereço IP
     */
    public String getIp() {
        return ip;
    }

    /**
     * Define o endereço IP.
     *
     * @param ip Novo endereço IP
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * Retorna a porta configurada.
     *
     * @return Número da porta
     */
    public int getPort() {
        return port;
    }

    /**
     * Define a porta de comunicação.
     *
     * @param port Novo número da porta
     */
    public void setPort(int port) {
        this.port = port;
    }
}