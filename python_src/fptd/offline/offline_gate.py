"""
离线门基类
"""

from abc import ABC, abstractmethod
from typing import List, TYPE_CHECKING

if TYPE_CHECKING:
    from .offline_circuit import OfflineCircuit
    from .fake_party import FakeParty


class OfflineGate(ABC):
    """离线门抽象基类"""
    
    def __init__(self, output_size: int):
        """
        Args:
            output_size: 输出大小
        """
        self.output_size = output_size
        self.input_gates: List['OfflineGate'] = []
        self.circuit: 'OfflineCircuit' = None
        self.gate_id: int = -1
    
    def set_circuit(self, circuit: 'OfflineCircuit', gate_id: int):
        """设置所属电路"""
        self.circuit = circuit
        self.gate_id = gate_id
    
    def get_fake_party(self) -> 'FakeParty':
        """获取FakeParty"""
        return self.circuit.fake_party
    
    def add_input_gate(self, gate: 'OfflineGate'):
        """添加输入门"""
        self.input_gates.append(gate)
    
    @abstractmethod
    def run_offline(self):
        """执行离线计算"""
        pass


class OfflineInputGate(OfflineGate):
    """离线输入门"""
    
    def run_offline(self):
        """生成输入门的随机数份额"""
        fake_party = self.get_fake_party()
        for _ in range(self.output_size):
            shares = fake_party.generate_random_shares()
            fake_party.add_shares_to_parties(shares)


class OfflineOutputGate(OfflineGate):
    """离线输出门"""
    
    def __init__(self, input_gate: OfflineGate):
        super().__init__(input_gate.output_size)
        self.add_input_gate(input_gate)
    
    def run_offline(self):
        """输出门不需要额外的离线数据"""
        fake_party = self.get_fake_party()
        for _ in range(self.output_size):
            shares = fake_party.generate_zero_shares()
            fake_party.add_shares_to_parties(shares)


class OfflineAddGate(OfflineGate):
    """离线加法门"""
    
    def __init__(self, input_gate1: OfflineGate, input_gate2: OfflineGate):
        super().__init__(input_gate1.output_size)
        self.add_input_gate(input_gate1)
        self.add_input_gate(input_gate2)
    
    def run_offline(self):
        """加法门不需要额外的离线数据，只需要λ"""
        fake_party = self.get_fake_party()
        for _ in range(self.output_size):
            shares = fake_party.generate_random_shares()
            fake_party.add_shares_to_parties(shares)


class OfflineSubtractGate(OfflineGate):
    """离线减法门"""
    
    def __init__(self, input_gate1: OfflineGate, input_gate2: OfflineGate):
        super().__init__(input_gate1.output_size)
        self.add_input_gate(input_gate1)
        self.add_input_gate(input_gate2)
    
    def run_offline(self):
        fake_party = self.get_fake_party()
        for _ in range(self.output_size):
            shares = fake_party.generate_random_shares()
            fake_party.add_shares_to_parties(shares)


class OfflineScalingGate(OfflineGate):
    """离线缩放门"""
    
    def __init__(self, input_gate: OfflineGate, scalar: int):
        super().__init__(input_gate.output_size)
        self.add_input_gate(input_gate)
        self.scalar = scalar
    
    def run_offline(self):
        fake_party = self.get_fake_party()
        for _ in range(self.output_size):
            shares = fake_party.generate_random_shares()
            fake_party.add_shares_to_parties(shares)


class OfflineAddConstantGate(OfflineGate):
    """离线常数加法门"""
    
    def __init__(self, input_gate: OfflineGate, constant: int):
        super().__init__(input_gate.output_size)
        self.add_input_gate(input_gate)
        self.constant = constant
    
    def run_offline(self):
        fake_party = self.get_fake_party()
        for _ in range(self.output_size):
            shares = fake_party.generate_random_shares()
            fake_party.add_shares_to_parties(shares)


class OfflineDotProductGate(OfflineGate):
    """离线点积门"""
    
    def __init__(self, input_gate1: OfflineGate, input_gate2: OfflineGate):
        super().__init__(1)
        self.add_input_gate(input_gate1)
        self.add_input_gate(input_gate2)
        self.input_size = input_gate1.output_size
    
    def run_offline(self):
        fake_party = self.get_fake_party()
        
        # λ份额
        shares = fake_party.generate_random_shares()
        fake_party.add_shares_to_parties(shares)
        
        # Beaver三元组
        for _ in range(self.input_size):
            a_shares, b_shares, c_shares = fake_party.generate_beaver_triple()
            fake_party.add_shares_to_parties(a_shares)
        
        for _ in range(self.input_size):
            a_shares, b_shares, c_shares = fake_party.generate_beaver_triple()
            fake_party.add_shares_to_parties(b_shares)
        
        # c = sum(a[i] * b[i])
        a_shares, b_shares, c_shares = fake_party.generate_beaver_triple()
        fake_party.add_shares_to_parties(c_shares)


class OfflineElemWiseMultGate(OfflineGate):
    """离线逐元素乘法门"""
    
    def __init__(self, input_gate1: OfflineGate, input_gate2: OfflineGate):
        super().__init__(input_gate1.output_size)
        self.add_input_gate(input_gate1)
        self.add_input_gate(input_gate2)
    
    def run_offline(self):
        fake_party = self.get_fake_party()
        
        # λ份额
        for _ in range(self.output_size):
            shares = fake_party.generate_random_shares()
            fake_party.add_shares_to_parties(shares)
        
        # Beaver三元组
        for _ in range(self.output_size):
            a_shares, _, _ = fake_party.generate_beaver_triple()
            fake_party.add_shares_to_parties(a_shares)
        
        for _ in range(self.output_size):
            _, b_shares, _ = fake_party.generate_beaver_triple()
            fake_party.add_shares_to_parties(b_shares)
        
        for _ in range(self.output_size):
            _, _, c_shares = fake_party.generate_beaver_triple()
            fake_party.add_shares_to_parties(c_shares)


class OfflineDivisionGate(OfflineGate):
    """离线除法门"""
    
    def __init__(self, input_gate: OfflineGate, divisor: int):
        super().__init__(input_gate.output_size)
        self.add_input_gate(input_gate)
        self.divisor = divisor
    
    def run_offline(self):
        from ..params import Params
        fake_party = self.get_fake_party()
        
        # λ份额
        for _ in range(self.output_size):
            shares = fake_party.generate_random_shares()
            fake_party.add_shares_to_parties(shares)
        
        # 除法随机数
        for _ in range(self.output_size):
            r_shares, r1_shares, r2_shares = fake_party.generate_division_randoms(
                Params.DIV_L, Params.DIV_SIGMA
            )
            fake_party.add_shares_to_parties(r_shares)
        
        for _ in range(self.output_size):
            r_shares, r1_shares, r2_shares = fake_party.generate_division_randoms(
                Params.DIV_L, Params.DIV_SIGMA
            )
            fake_party.add_shares_to_parties(r1_shares)
        
        for _ in range(self.output_size):
            r_shares, r1_shares, r2_shares = fake_party.generate_division_randoms(
                Params.DIV_L, Params.DIV_SIGMA
            )
            fake_party.add_shares_to_parties(r2_shares)


class OfflineDotProdThenDivGate(OfflineGate):
    """离线点积后除法门"""
    
    def __init__(self, input_gate1: OfflineGate, input_gate2: OfflineGate, divisor_gate: OfflineGate):
        super().__init__(1)
        self.add_input_gate(input_gate1)
        self.add_input_gate(input_gate2)
        self.add_input_gate(divisor_gate)
        self.input_size = input_gate1.output_size
    
    def run_offline(self):
        from ..params import Params
        fake_party = self.get_fake_party()
        
        # λ份额
        shares = fake_party.generate_random_shares()
        fake_party.add_shares_to_parties(shares)
        
        # Beaver三元组
        for _ in range(self.input_size):
            a_shares, _, _ = fake_party.generate_beaver_triple()
            fake_party.add_shares_to_parties(a_shares)
        
        for _ in range(self.input_size):
            _, b_shares, _ = fake_party.generate_beaver_triple()
            fake_party.add_shares_to_parties(b_shares)
        
        _, _, c_shares = fake_party.generate_beaver_triple()
        fake_party.add_shares_to_parties(c_shares)
        
        # 除法随机数
        r_shares, r1_shares, r2_shares = fake_party.generate_division_randoms(
            Params.DIV_L, Params.DIV_SIGMA
        )
        fake_party.add_shares_to_parties(r_shares)
        fake_party.add_shares_to_parties(r1_shares)
        fake_party.add_shares_to_parties(r2_shares)


class OfflineDotProdWithFilterGate(OfflineGate):
    """离线带过滤器的点积门"""
    
    def __init__(self, input_gate1: OfflineGate, input_gate2: OfflineGate, filter_vec: List[bool]):
        super().__init__(1)
        self.add_input_gate(input_gate1)
        self.add_input_gate(input_gate2)
        self.filter_vec = filter_vec
        self.valid_count = sum(filter_vec)
    
    def run_offline(self):
        fake_party = self.get_fake_party()
        
        # λ份额
        shares = fake_party.generate_random_shares()
        fake_party.add_shares_to_parties(shares)
        
        # Beaver三元组 (只对有效元素)
        for _ in range(self.valid_count):
            a_shares, _, _ = fake_party.generate_beaver_triple()
            fake_party.add_shares_to_parties(a_shares)
        
        for _ in range(self.valid_count):
            _, b_shares, _ = fake_party.generate_beaver_triple()
            fake_party.add_shares_to_parties(b_shares)
        
        _, _, c_shares = fake_party.generate_beaver_triple()
        fake_party.add_shares_to_parties(c_shares)


class OfflineLogGate(OfflineGate):
    """离线对数门"""
    
    def __init__(self, input_gate: OfflineGate):
        super().__init__(input_gate.output_size)
        self.add_input_gate(input_gate)
    
    def run_offline(self):
        fake_party = self.get_fake_party()
        for _ in range(self.output_size):
            shares = fake_party.generate_random_shares()
            fake_party.add_shares_to_parties(shares)


class OfflineCombinationGate(OfflineGate):
    """离线组合门"""
    
    def __init__(self, input_gates: List[OfflineGate]):
        total_size = sum(g.output_size for g in input_gates)
        super().__init__(total_size)
        for gate in input_gates:
            self.add_input_gate(gate)
    
    def run_offline(self):
        fake_party = self.get_fake_party()
        for _ in range(self.output_size):
            shares = fake_party.generate_random_shares()
            fake_party.add_shares_to_parties(shares)


class OfflineReduceGate(OfflineGate):
    """离线归约门"""
    
    def __init__(self, input_gate: OfflineGate):
        super().__init__(1)
        self.add_input_gate(input_gate)
    
    def run_offline(self):
        fake_party = self.get_fake_party()
        shares = fake_party.generate_random_shares()
        fake_party.add_shares_to_parties(shares)
