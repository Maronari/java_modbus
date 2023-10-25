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
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters;
import com.intelligt.modbus.jlibmodbus.serial.SerialPort;
import com.intelligt.modbus.jlibmodbus.tcp.TcpParameters;

import jssc.SerialPortList;

public class ModbusSurvey {

    public static int convertToSigned16Bit(int unsignedValue) {
        if ((unsignedValue & (1 << 15)) != 0) { // if the sign bit is set, the number is negative
            return unsignedValue - (1 << 16); // convert to negative number
        } else {
            return unsignedValue; // number is positive, return as is
        }
    }

    public static void main(String[] args) {
        try {
            String[] dev_list = SerialPortList.getPortNames();
            //System.out.println("COM's: ");
            for (String string : dev_list) {
                System.out.print(string + " ");
            }

            TcpParameters tcpParameters = new TcpParameters();
            SerialParameters serialParameters = new SerialParameters();

            // serial: COM1, 9600, 8
            // serialParameters.setDevice("COM2");
            serialParameters.setBaudRate(SerialPort.BaudRate.BAUD_RATE_9600);
            serialParameters.setDataBits(8);
            serialParameters.setParity(SerialPort.Parity.NONE);
            serialParameters.setStopBits(1);

            // tcp: 127.0.0.1:502
            tcpParameters.setHost(InetAddress.getLocalHost());
            tcpParameters.setKeepAlive(true);
            tcpParameters.setPort(Modbus.TCP_PORT);

            ModbusMaster master = ModbusMasterFactory.createModbusMasterTCP(tcpParameters);
            Modbus.setAutoIncrementTransactionId(true);
            master.setResponseTimeout(100);

            // ModbusMaster master =
            // ModbusMasterFactory.createModbusMasterRTU(serialParameters);
            // master.connect();

            int slaveId = 0;
            int quantity = 2;
            try {
                if (!master.isConnected()) {
                    master.connect();
                }

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
                Date date = new Date(System.currentTimeMillis());
                System.out.println(formatter.format(date));

                while (true) {
                    while (slaveId < 255) {
                        try {
                            slaveId++;
                            int offset = 0;
                            int[] registerValues = master.readInputRegisters(slaveId, offset, quantity);
                            for (int i = 1; i < registerValues.length; i += 2) {
                                // int value = registerValues[i];
                                System.out.println("Slave: " + slaveId);
                                offset += 2;
                            }
                        } catch (ModbusIOException e) {
                            continue;
                        }
                    }
                    slaveId = 0;
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
                    master.disconnect();
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
