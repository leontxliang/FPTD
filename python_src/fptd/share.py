"""
秘密份额数据结构
"""

from .params import Params


class Share:
    """表示Shamir秘密共享中的一个份额"""
    
    def __init__(self, party_id: int, shr: int):
        """
        Args:
            party_id: 参与方ID (从0开始)
            shr: 份额值
        """
        self.party_id = party_id
        self.shr = shr % Params.P
    
    def add(self, other: 'Share') -> 'Share':
        """份额加法"""
        if self.party_id != other.party_id:
            raise ValueError("Cannot add shares from different parties")
        return Share(self.party_id, (self.shr + other.shr) % Params.P)
    
    def subtract(self, other: 'Share') -> 'Share':
        """份额减法"""
        if self.party_id != other.party_id:
            raise ValueError("Cannot subtract shares from different parties")
        return Share(self.party_id, (self.shr - other.shr) % Params.P)
    
    def multiply(self, c: int) -> 'Share':
        """常数乘法"""
        return Share(self.party_id, (self.shr * c) % Params.P)
    
    def __add__(self, other: 'Share') -> 'Share':
        return self.add(other)
    
    def __sub__(self, other: 'Share') -> 'Share':
        return self.subtract(other)
    
    def __mul__(self, c: int) -> 'Share':
        return self.multiply(c)
    
    def __rmul__(self, c: int) -> 'Share':
        return self.multiply(c)
    
    def __repr__(self):
        return f"Share(party_id={self.party_id}, shr={self.shr})"
    
    def __eq__(self, other):
        if not isinstance(other, Share):
            return False
        return self.party_id == other.party_id and self.shr == other.shr
    
    def copy(self) -> 'Share':
        """创建副本"""
        return Share(self.party_id, self.shr)
