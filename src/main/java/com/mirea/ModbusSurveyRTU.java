package com.mirea;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException;
import com.intelligt.modbus.jlibmodbus.exception.ModbusNumberException;
import com.intelligt.modbus.jlibmodbus.exception.ModbusProtocolException;
import com.intelligt.modbus.jlibmodbus.master.ModbusMaster;
import com.intelligt.modbus.jlibmodbus.master.ModbusMasterFactory;
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters;
import com.intelligt.modbus.jlibmodbus.serial.SerialPort;

import jssc.SerialPortList;

/****************************************************************
 * get connection to serial port and read read all registers of slave
 * @NOTWORK
*****************************************************************/
public class ModbusSurveyRTU {

    public static void main(String[] args) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());

        try {
            String[] dev_list = SerialPortList.getPortNames();
            System.out.print("COM's: ");
            for (String string : dev_list) {
                System.out.print(string + " ");
            }
            SerialParameters serialParameters = new SerialParameters();

            // get serial connection parameters
            serialParameters.setDevice(dev_list[0]);
            serialParameters.setDataBits(8);
            serialParameters.setParity(SerialPort.Parity.NONE);
            serialParameters.setBaudRate(SerialPort.BaudRate.BAUD_RATE_9600);
            serialParameters.setStopBits(1);

            ModbusMaster master = ModbusMasterFactory.createModbusMasterRTU(serialParameters);
            master.setResponseTimeout(100);
            master.connect();
            int slaveId = 0;
            int quantity = 2;

            try {
                while (true) {
                    System.out.println(formatter.format(date));
                    for (SerialPort.BaudRate rate : SerialPort.BaudRate.values()) {
                        serialParameters.setBaudRate(rate);                                                 //change baud rate
                        System.out.println("BaudRate: " + serialParameters.getBaudRate());

                        master = ModbusMasterFactory.createModbusMasterRTU(serialParameters);               //create master
                        master.setResponseTimeout(100);
                        master.connect();

                        while (slaveId < 255) {                                                             //iteration over all slaves
                            try {
                                slaveId++;
                                int offset = 0;
                                int[] registerValues = master.readInputRegisters(slaveId, offset, quantity);//read register values of slave
                                System.out.println("*********************************************************");
                                for (int i = 1; i < registerValues.length; i += 2) {
                                    System.out.println("Slave: " + slaveId);
                                    System.out.println("Register: " + i + " " + registerValues[i]);
                                    offset += 2;
                                }
                                System.out.println("*********************************************************");
                            } catch (ModbusIOException e) {
                                continue;
                            }
                        }
                        slaveId = 0;
                        System.out.println("--------------------------------------------------------");
                        Thread.sleep(2000);
                    }
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
