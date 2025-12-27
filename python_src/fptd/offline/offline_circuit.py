"""
离线电路类
"""

from typing import List
from .fake_party import FakeParty
from .offline_gate import (
    OfflineGate, OfflineInputGate, OfflineOutputGate,
    OfflineAddGate, OfflineSubtractGate, OfflineScalingGate, OfflineAddConstantGate,
    OfflineDotProductGate, OfflineElemWiseMultGate, OfflineDivisionGate,
    OfflineDotProdThenDivGate, OfflineDotProdWithFilterGate,
    OfflineLogGate, OfflineCombinationGate, OfflineReduceGate
)


class OfflineCircuit:
    """离线计算电路"""
    
    def __init__(self, job_name: str):
        """
        Args:
            job_name: 任务名称
        """
        self.job_name = job_name
        self.gates: List[OfflineGate] = []
        self.fake_party = FakeParty(job_name)
    
    def add_gate(self, gate: OfflineGate) -> OfflineGate:
        """添加门到电路"""
        gate.set_circuit(self, len(self.gates))
        self.gates.append(gate)
        return gate
    
    # ==================== 门构建方法 ====================
    
    def input(self, size: int) -> OfflineInputGate:
        """创建输入门"""
        gate = OfflineInputGate(size)
        return self.add_gate(gate)
    
    def output(self, input_gate: OfflineGate) -> OfflineOutputGate:
        """创建输出门"""
        gate = OfflineOutputGate(input_gate)
        return self.add_gate(gate)
    
    def add(self, gate1: OfflineGate, gate2: OfflineGate) -> OfflineAddGate:
        """创建加法门"""
        gate = OfflineAddGate(gate1, gate2)
        return self.add_gate(gate)
    
    def subtract(self, gate1: OfflineGate, gate2: OfflineGate) -> OfflineSubtractGate:
        """创建减法门"""
        gate = OfflineSubtractGate(gate1, gate2)
        return self.add_gate(gate)
    
    def scale(self, gate: OfflineGate, scalar: int) -> OfflineScalingGate:
        """创建缩放门"""
        g = OfflineScalingGate(gate, scalar)
        return self.add_gate(g)
    
    def add_constant(self, gate: OfflineGate, constant: int) -> OfflineAddConstantGate:
        """创建常数加法门"""
        g = OfflineAddConstantGate(gate, constant)
        return self.add_gate(g)
    
    def dot_product(self, gate1: OfflineGate, gate2: OfflineGate) -> OfflineDotProductGate:
        """创建点积门"""
        gate = OfflineDotProductGate(gate1, gate2)
        return self.add_gate(gate)
    
    def elem_wise_multiply(self, gate1: OfflineGate, gate2: OfflineGate) -> OfflineElemWiseMultGate:
        """创建逐元素乘法门"""
        gate = OfflineElemWiseMultGate(gate1, gate2)
        return self.add_gate(gate)
    
    def division(self, gate: OfflineGate, divisor: int) -> OfflineDivisionGate:
        """创建除法门"""
        g = OfflineDivisionGate(gate, divisor)
        return self.add_gate(g)
    
    def dot_prod_then_div(self, gate1: OfflineGate, gate2: OfflineGate, 
                          divisor_gate: OfflineGate) -> OfflineDotProdThenDivGate:
        """创建点积后除法门"""
        gate = OfflineDotProdThenDivGate(gate1, gate2, divisor_gate)
        return self.add_gate(gate)
    
    def dot_prod_with_filter(self, gate1: OfflineGate, gate2: OfflineGate,
                             filter_vec: List[bool]) -> OfflineDotProdWithFilterGate:
        """创建带过滤器的点积门"""
        gate = OfflineDotProdWithFilterGate(gate1, gate2, filter_vec)
        return self.add_gate(gate)
    
    def logarithm(self, gate: OfflineGate) -> OfflineLogGate:
        """创建对数门"""
        g = OfflineLogGate(gate)
        return self.add_gate(g)
    
    def combination(self, gates: List[OfflineGate]) -> OfflineCombinationGate:
        """创建组合门"""
        gate = OfflineCombinationGate(gates)
        return self.add_gate(gate)
    
    def reduce(self, gate: OfflineGate) -> OfflineReduceGate:
        """创建归约门"""
        g = OfflineReduceGate(gate)
        return self.add_gate(g)
    
    # ==================== 执行方法 ====================
    
    def run_offline(self):
        """执行离线计算并写入文件"""
        for gate in self.gates:
            gate.run_offline()
        
        self.fake_party.write_to_files()
