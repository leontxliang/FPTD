"""
对数门
"""

import math
from typing import List
from .gate import Gate
from ..params import Params
from ..share import Share


class LogarithmGate(Gate):
    """对数门 - 使用泰勒展开近似"""
    
    def __init__(self, input_gate: Gate):
        """
        Args:
            input_gate: 输入门
        """
        super().__init__(input_gate.output_size)
        self.add_input_gate(input_gate)
    
    def run_online(self):
        """
        执行对数计算 (近似)
        
        使用预计算的查找表或泰勒展开
        这里简化为直接计算 (假设输入已公开)
        """
        server = self.get_server()
        party_id = self.get_party_id()
        
        lambdas = self.get_input_lambda_shares()
        deltas = self.get_input_delta_clears()
        
        n = self.output_size
        
        # 获取输入值
        input_values = []
        for i in range(n):
            val = (deltas[i] - lambdas[i].shr) % Params.P
            # 转换为有符号数
            if val > Params.P // 2:
                val = val - Params.P
            input_values.append(val)
        
        # 计算对数 (定点数)
        output_values = []
        for val in input_values:
            if val <= 0:
                # 处理非正数
                log_val = 0
            else:
                # 转换为浮点数计算对数
                float_val = val / Params.PRECISE_ROUND
                if float_val > 0:
                    log_float = math.log(float_val)
                    log_val = int(log_float * Params.PRECISE_ROUND) % Params.P
                else:
                    log_val = 0
            output_values.append(log_val)
        
        # 设置输出 (这里简化处理，实际应该用秘密共享)
        self.delta_clear_list = output_values
        self.lambda_share_list = [Share(party_id, 0) for _ in range(n)]
