package com.mirea;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.intelligt.modbus.jlibmodbus.Modbus;
import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException;
import com.intelligt.modbus.jlibmodbus.exception.ModbusNumberException;
import com.intelligt.modbus.jlibmodbus.exception.ModbusProtocolException;
import com.intelligt.modbus.jlibmodbus.master.ModbusMaster;
import com.intelligt.modbus.jlibmodbus.master.ModbusMasterFactory;
import com.intelligt.modbus.jlibmodbus.tcp.TcpParameters;


/****************************************************************
 * get connection to remote server and read read all registers of slave 
*****************************************************************/
public class ModbusTCPOneSlave {

    public static int convertToSigned16Bit(int unsignedValue) {
        if ((unsignedValue & (1 << 15)) != 0) { // if the sign bit is set, the number is negative
            return unsignedValue - (1 << 16); // convert to negative number
        } else {
            return unsignedValue; // number is positive, return as is
        }
    }

    public static void main(String[] args) {
        try {
            TcpParameters tcpParameters = new TcpParameters();

            // tcp: 127.0.0.1:502
            tcpParameters.setHost(InetAddress.getLocalHost());
            tcpParameters.setKeepAlive(true);
            tcpParameters.setPort(Modbus.TCP_PORT);

            // create master
            ModbusMaster m = ModbusMasterFactory.createModbusMasterTCP(tcpParameters);
            Modbus.setAutoIncrementTransactionId(true);

            int slaveId = 1;
            int quantity = 16;

            try {
                if (!m.isConnected()) {
                    m.connect();
                }

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");

                while (true) {

                    int offset = 0;
                    // receive registers from a slave with id of ID 1 at offset of 0
                    int[] registerValues = m.readInputRegisters(slaveId, offset, quantity);

                    Date date = new Date(System.currentTimeMillis());
                    System.out.println(formatter.format(date));

                    for (int i = 1; i < registerValues.length; i += 2) {
                        int value = registerValues[i];
                        System.out.println("Address: " + offset + ", Value: " + (convertToSigned16Bit(value)));
                        offset += 2;
                    }

                    System.out.println("--------------------------------------------------------");
                    Thread.sleep(2000);
                }
            } catch (ModbusProtocolException e) {
                e.printStackTrace();
            } catch (ModbusNumberException e) {
                e.printStackTrace();
            } catch (ModbusIOException e) {
                e.printStackTrace();
            } finally {
                try {
                    m.disconnect();
                } catch (ModbusIOException e) {
                    e.printStackTrace();
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
