package org.coffee.services;

import org.coffee.domain.models.SiemensS7Protocol;

/**
 * Despachante de operacoes Siemens S7 por nome do tipo de dado.
 *
 * Centraliza a logica de "dado um dataType em String, leia/escreva
 * a variavel correta no SiemensS7Protocol". Existia antes duplicada
 * nos controllers; foi extraida para:
 *   - manter os controllers focados em HTTP (request -> response)
 *   - evitar divergencia entre SiemensS7Controller (avulso) e
 *     SiemensS7OperationController (cadastrado)
 *   - facilitar adicao de novos tipos no futuro (so muda um lugar)
 *
 * Aceita sinonimos para cobrir tanto a nomenclatura nativa Siemens
 * (BOOL, REAL, DINT) quanto a nomenclatura generica do projeto
 * (BOOLEAN, FLOAT, INT).
 *
 * Classe utilitaria: nao tem estado, nao deve ser instanciada,
 * todos os metodos sao estaticos.
 */
public final class SiemensS7DataTypeDispatcher {

    private SiemensS7DataTypeDispatcher() {
        // utilitaria - nao instanciar
    }

    /**
     * Le uma variavel S7 com base no nome do tipo de dado.
     *
     * @param protocol   protocol ja com conexao aberta
     * @param dataType   nome do tipo (case insensitive). Aceita: BOOL/BOOLEAN,
     *                   BYTE, WORD, DWORD, INT/INTEGER, DINT, REAL/FLOAT, STRING
     * @param dbNumber   numero da DB no CLP
     * @param offset     byte offset dentro da DB
     * @param bitOffset  bit offset (so usado para BOOL; pode ser null)
     * @return valor lido com tipo Java apropriado para o dataType
     * @throws IllegalArgumentException se o dataType nao for suportado
     */
    public static Object read(SiemensS7Protocol protocol,
                              String dataType,
                              Integer dbNumber,
                              Integer offset,
                              Integer bitOffset) {
        String t = normalize(dataType);
        int bit = bitOffset != null ? bitOffset : 0;
        switch (t) {
            case "BOOL":
            case "BOOLEAN":
                return protocol.readBool(dbNumber, offset, bit);
            case "BYTE":
                return protocol.readByte(dbNumber, offset);
            case "WORD":
                return protocol.readWord(dbNumber, offset);
            case "DWORD":
                return protocol.readDWord(dbNumber, offset);
            case "INT":
            case "INTEGER":
                return protocol.readInt(dbNumber, offset);
            case "DINT":
                return protocol.readDInt(dbNumber, offset);
            case "REAL":
            case "FLOAT":
                return protocol.readReal(dbNumber, offset);
            case "STRING":
                return protocol.readString(dbNumber, offset);
            default:
                throw new IllegalArgumentException(unsupportedMessage(dataType));
        }
    }

    /**
     * Escreve em uma variavel S7 com base no nome do tipo de dado.
     * Faz a conversao da String recebida via HTTP para o tipo Java apropriado.
     *
     * @param protocol   protocol ja com conexao aberta
     * @param dataType   nome do tipo (case insensitive). Mesmos suportados do read.
     * @param dbNumber   numero da DB no CLP
     * @param offset     byte offset dentro da DB
     * @param bitOffset  bit offset (so usado para BOOL; pode ser null)
     * @param value      valor em String para converter ao tipo apropriado
     * @throws IllegalArgumentException se o dataType nao for suportado
     * @throws NumberFormatException    se o value nao puder ser convertido
     */
    public static void write(SiemensS7Protocol protocol,
                             String dataType,
                             Integer dbNumber,
                             Integer offset,
                             Integer bitOffset,
                             String value) {
        String t = normalize(dataType);
        int bit = bitOffset != null ? bitOffset : 0;
        switch (t) {
            case "BOOL":
            case "BOOLEAN":
                protocol.writeBool(dbNumber, offset, bit, Boolean.parseBoolean(value));
                break;
            case "BYTE":
                protocol.writeByte(dbNumber, offset, Byte.parseByte(value));
                break;
            case "WORD":
                protocol.writeWord(dbNumber, offset, Integer.parseInt(value));
                break;
            case "DWORD":
                protocol.writeDWord(dbNumber, offset, Long.parseLong(value));
                break;
            case "INT":
            case "INTEGER":
                protocol.writeInt(dbNumber, offset, Integer.parseInt(value));
                break;
            case "DINT":
                protocol.writeDInt(dbNumber, offset, Integer.parseInt(value));
                break;
            case "REAL":
            case "FLOAT":
                protocol.writeReal(dbNumber, offset, Float.parseFloat(value));
                break;
            case "STRING":
                protocol.writeString(dbNumber, offset, value);
                break;
            default:
                throw new IllegalArgumentException(unsupportedMessage(dataType));
        }
    }

    private static String normalize(String dataType) {
        return dataType == null ? "" : dataType.toUpperCase();
    }

    private static String unsupportedMessage(String dataType) {
        return "Tipo de dado nao suportado para Siemens S7: " + dataType
                + ". Tipos aceitos: BOOL, BYTE, WORD, DWORD, INT, DINT, REAL, STRING.";
    }
}
