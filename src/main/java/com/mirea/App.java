package com.mirea;

import java.util.Arrays;

import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersRequest;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse;
import com.serotonin.modbus4j.serial.SerialPortWrapper;

public class App {

    static int slaveId = 1;

    public static void main(String[] args) {
        String commPortId = "COM2";
        int baudRate = 9600;
        int flowControlIn = 0;
        int flowControlOut = 0;

        int dataBits = 8;
        int stopBits = 0;
        int parity = 1;

        SerialPortWrapper serialParam = new SerialPortWrapperImpl(commPortId, baudRate, dataBits, stopBits, parity,
                flowControlIn, flowControlOut);

        ModbusFactory factory = new ModbusFactory();
        ModbusMaster master = factory.createRtuMaster(serialParam);

        try {
            master.init();
            readHoldingRegistersTest(master, slaveId, 0, 24);
        } catch (ModbusInitException e) {
            e.printStackTrace();
        } finally {
            master.destroy();
        }
    }

    private static void readHoldingRegistersTest(ModbusMaster master, int slaveId, int start, int len) {
        try {
            ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(slaveId, start, len);
            ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse)master.send(request);
            if (response.isException()) {
                System.out.println("Exception response: message=" + response.getExceptionMessage());
            } else {
                System.out.println(Arrays.toString(response.getShortData()));
                short[] list = response.getShortData();
                for (int i = 0; i < list.length; i++) {
                    System.out.print(list[i] + " ");
                }
            }
        } catch (ModbusTransportException e) {
            e.printStackTrace();
        }
    }
}
