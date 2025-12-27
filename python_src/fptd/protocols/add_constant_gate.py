"""
常数加法门
"""

from typing import List
from .gate import Gate
from ..params import Params
from ..share import Share


class AddConstantGate(Gate):
    """常数加法门 - 加常数 (本地计算，无通信)"""
    
    def __init__(self, input_gate: Gate, constant: int):
        """
        Args:
            input_gate: 输入门
            constant: 常数
        """
        super().__init__(input_gate.output_size)
        self.add_input_gate(input_gate)
        self.constant = constant % Params.P
    
    def run_online(self):
        """执行常数加法 (本地计算)"""
        lambdas = self.get_input_lambda_shares()
        deltas = self.get_input_delta_clears()
        
        # λ 不变
        self.lambda_share_list = [l.copy() for l in lambdas]
        
        # Δ_out = Δ + c (只有第一个元素加常数)
        self.delta_clear_list = deltas.copy()
        if self.delta_clear_list:
            self.delta_clear_list[0] = (self.delta_clear_list[0] + self.constant) % Params.P
