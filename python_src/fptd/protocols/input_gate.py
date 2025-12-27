"""
输入门
"""

from typing import List
from .gate import Gate
from ..params import Params
from ..share import Share


class InputGate(Gate):
    """输入门 - 处理秘密输入"""
    
    def __init__(self, output_size: int, input_values: List[int] = None):
        """
        Args:
            output_size: 输出大小
            input_values: 输入值 (只有数据持有者需要提供)
        """
        super().__init__(output_size)
        self.input_values = input_values or [0] * output_size
    
    def set_input_values(self, values: List[int]):
        """设置输入值"""
        self.input_values = values
    
    def run_online(self):
        """
        执行输入门协议
        
        协议流程:
        1. 数据持有者计算 Δ = x + λ 并广播
        2. 其他参与方接收 Δ
        """
        server = self.get_server()
        party_id = self.get_party_id()
        
        # 计算 Δ = x + λ
        self.delta_clear_list = []
        for i in range(self.output_size):
            delta = (self.input_values[i] + self.lambda_share_list[i].shr) % Params.P
            self.delta_clear_list.append(delta)
        
        # 广播 Δ (通过King服务器中转)
        if server.is_king:
            # King收集所有参与方的Δ并广播
            all_deltas = server.king_read_from_all(self.output_size)
            # 计算总和 (所有参与方的Δ相同，这里简化处理)
            self.delta_clear_list = all_deltas[0]  # 使用第一个参与方的值
            server.king_send_to_all(self.delta_clear_list)
        else:
            server.send_to_king(self.delta_clear_list)
            self.delta_clear_list = server.read_from_king(self.output_size)
