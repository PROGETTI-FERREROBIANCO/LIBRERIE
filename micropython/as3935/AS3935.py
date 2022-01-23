import time

class AS3935:
    def __init__(self, i2c_address_device, i2c_object):
        self.i2c_bus = i2c_object
        self.i2c_address_device = i2c_address_device
        self.is_indoor = True
        self.values_srej = ["0000","0001","0010","0011","0100","0101","0110","0111","1000","1001","1010","1011"]
        self.values_wdth = ["0000","0001","0010","0011","0100","0101","0110","0111","1000","1001","1010"]
        self.values_division_radio = {16:"00", 32:"01", 64:"10", 128:"11"}
        self.values_min_num_lightning = {1:"00", 5:"01", 9:"10", 16:"11"}
        self.values_nf_level_indoor = {28:"000", 45:"001", 62:"010", 78:"011", 95:"100", 112:"101", 130:"110", 146:"111"}
        self.values_nf_level_outdoor = {390:"000", 630:"001", 860:"010", 1100:"011", 1140:"100", 1570:"101", 1800:"110", 2000:"111"}
        self.INT_NH = 1
        self.INT_D = 4
        self.INT_L = 8
    
    def set_sensor_indoor(self):
        self.is_indoor = True
        self.__send_byte(0x00, "10010", 2)
    
    def set_sensor_outdoor(self):
        self.is_indoor = False
        self.__send_byte(0x00, "01110", 2)
    
    def set_irq_pin_transmit_frequency_trco_oscillator(self):
        self.__send_byte(0x08, "001")
    
    def set_irq_pin_transmit_frequency_srco_oscillator(self):
        self.__send_byte(0x08, "010")
    
    def set_irq_pin_transmit_frequency_antenna_resonance(self):
        self.__send_byte(0x08, "100")

    def set_irq_pin_for_interrupt_signal(self):
        self.__send_byte(0x08, "000")

    def set_mask_disturber(self):
        self.__send_byte(0x03, "1", 2)

    def remove_mask_disturber(self):
        self.__send_byte(0x03, "0", 2)

    def set_division_radio(self, value_div_radio):
        if value_div_radio in self.values_division_radio:
            self.__send_byte(0x03, self.values_division_radio[value_div_radio])

    def set_level_of_srej(self, index_values_srej=2):
        if index_values_srej < len(self.values_srej) and index_values_srej > 0: 
            self.__send_byte(0x02, self.values_srej[index_values_srej], 4)
    
    def set_min_num_lightning(self, value_min_num_ligh):
        if value_min_num_ligh in self.values_min_num_lightning:
            self.__send_byte(0x02, self.values_min_num_lightning[value_min_num_ligh], 2)

    def set_level_of_wdth(self, index_values_wdth=2):
        if index_values_wdth < len(self.values_wdth) and index_values_wdth > 0: 
            self.__send_byte(0x01, self.values_wdth[index_values_wdth], 4)
    
    def set_noisy_floor_level(self, value_nf_lev=62):
        if self.is_indoor:
            if value_nf_lev in self.values_nf_level_indoor:
                self.__send_byte(0x01, self.values_nf_level_indoor[value_nf_lev], 1)
        else:
            if value_nf_lev in self.values_nf_level_outdoor:
                self.__send_byte(0x01, self.values_nf_level_outdoor[value_nf_lev], 1)
    
    def set_register_default(self, register="all"):
        if register == "all":
            self.__send_byte(0x00, "00100100")
            self.__send_byte(0x01, "0100010", 1)
            self.__send_byte(0x02, "11000010")
            self.__send_byte(0x03, "00000000")
            self.__send_byte(0x08, "000")
            self.__send_byte(0x08, "0000", 4)
        else:
            byte = "00100100"
            initial_index_changing_part = 0
            if register == 0x00: 
                byte = "00100100"
            elif register == 0x01: 
                byte = "0100010"
                initial_index_changing_part = 1
            elif register == 0x02: 
                byte = "11000010"
            elif register == 0x03: 
                byte = "00000000"
            elif register == 0x08: 
                self.__send_byte(0x08, "000")
                byte = "0000"
                initial_index_changing_part = 4
            
            self.__send_byte(register, byte, initial_index_changing_part)
                

    def print_content_of_registers(self):
        registers = {}
        for n_register in range(9):
            registers[n_register] = self.__convert_byte_to_string(self.i2c_bus.readfrom_mem(self.i2c_address_device, n_register, 1))
        for n_register in range(0x3A, 0X3C):
            registers[n_register] = self.__convert_byte_to_string(self.i2c_bus.readfrom_mem(self.i2c_address_device, n_register, 1))
        return registers
    
    def set_registers_manually(self, register, byte_to_send):
        self.i2c_bus.writeto_mem(self.i2c_address_device, register, int(byte_to_send, 2))
    
    def get_name_type_interrupt(self):
        ibyte = self.__convert_byte_to_string(self.i2c_bus.readfrom_mem(self.i2c_address_device, 0x03, 1))[-4:]
        return int(ibyte,2)
    
    def get_distance_estimation_lightning(self):
        dbyte = self.i2c_bus.readfrom_mem(self.i2c_address_device, 0x07, 1)
        if(dbyte == 63): return "Out of range"
        else: return f"km:{dbyte}"
    
    def get_energy_lightning(self):
        mmsbyte = self.__convert_byte_to_string(self.i2c_bus.readfrom_mem(self.i2c_address_device, 0x06, 1))[-5:]
        msbyte = self.__convert_byte_to_string(self.i2c_bus.readfrom_mem(self.i2c_address_device, 0x05, 1))
        lsbyte = self.__convert_byte_to_string(self.i2c_bus.readfrom_mem(self.i2c_address_device, 0x04, 1))

        return int(mmsbyte+msbyte+lsbyte, 2)

    def get_calibration_state_of_trco_oscillator(self):
        cbyte = self.__convert_byte_to_string(self.i2c_bus.readfrom_mem(self.i2c_address_device, 0x3A, 1))
        if(cbyte[0] == 1 ): return "done"
        elif(cbyte[1] == 1): return "unsuccessful"
        else: return "error"
    
    def get_calibration_state_of_srco_oscillator(self):
        cbyte = self.__convert_byte_to_string(self.i2c_bus.readfrom_mem(self.i2c_address_device, 0x3B, 1))
        if(cbyte[0] == 1 ): return "done"
        elif(cbyte[1] == 1): return "unsuccessful"
        else: return "error"


    def preset_default(self):
        self.i2c_bus.writeto_mem(self.i2c_address_device, 0x3C, 0x96)

    def calibration(self):
        self.i2c_bus.writeto_mem(self.i2c_address_device, 0x3D, 0x96)

    def power_down(self):
        self.__send_byte(0x00, "1", 7)
    
    def clean_statistic_data(self):
        self.__send_byte(0x02, "1", 1)
        time.sleep(0.01)
        self.__send_byte(0x02, "0", 1)
        time.sleep(0.01)
        self.__send_byte(0x02, "1", 1)
        time.sleep(0.01)

    def __send_byte(self, register, changed_part, initial_index_changing_part=0):
        element = self.__convert_byte_to_string(self.i2c_bus.readfrom_mem(self.i2c_address_device, register, 1))
        byte_to_send = element[0:initial_index_changing_part] + changed_part + element[len(changed_part)+initial_index_changing_part:]
        self.i2c_bus.writeto_mem(self.i2c_address_device, register, int(byte_to_send, 2))

    def __convert_byte_to_string(self, number):
        byte = bin(number).replace("0b", "")
        return '0'*(8-len(byte)) + byte