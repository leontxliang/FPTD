"""
线性代数工具类
"""

from typing import List, Optional
from ..params import Params
from ..share import Share


class LinearAlgebra:
    """向量和矩阵运算工具"""
    
    @staticmethod
    def add_shares_vec(vec1: List[Share], vec2: List[Share]) -> List[Share]:
        """份额向量加法"""
        if len(vec1) != len(vec2):
            raise ValueError("Vectors must have the same length")
        return [s1 + s2 for s1, s2 in zip(vec1, vec2)]
    
    @staticmethod
    def subtract_shares_vec(vec1: List[Share], vec2: List[Share]) -> List[Share]:
        """份额向量减法"""
        if len(vec1) != len(vec2):
            raise ValueError("Vectors must have the same length")
        return [s1 - s2 for s1, s2 in zip(vec1, vec2)]
    
    @staticmethod
    def add_bigint_vec(vec1: List[int], vec2: List[int]) -> List[int]:
        """整数向量加法"""
        if len(vec1) != len(vec2):
            raise ValueError("Vectors must have the same length")
        return [(v1 + v2) % Params.P for v1, v2 in zip(vec1, vec2)]
    
    @staticmethod
    def subtract_bigint_vec(vec1: List[int], vec2: List[int]) -> List[int]:
        """整数向量减法"""
        if len(vec1) != len(vec2):
            raise ValueError("Vectors must have the same length")
        return [(v1 - v2) % Params.P for v1, v2 in zip(vec1, vec2)]
    
    @staticmethod
    def elem_wise_multiply_shares(vec1: List[Share], vec2: List[Share]) -> List[Share]:
        """份额向量逐元素乘法 (仅用于本地计算，不是安全乘法)"""
        if len(vec1) != len(vec2):
            raise ValueError("Vectors must have the same length")
        return [Share(s1.party_id, (s1.shr * s2.shr) % Params.P) for s1, s2 in zip(vec1, vec2)]
    
    @staticmethod
    def elem_wise_multiply_bigint(vec1: List[int], vec2: List[int]) -> List[int]:
        """整数向量逐元素乘法"""
        if len(vec1) != len(vec2):
            raise ValueError("Vectors must have the same length")
        return [(v1 * v2) % Params.P for v1, v2 in zip(vec1, vec2)]
    
    @staticmethod
    def scale_shares_vec(vec: List[Share], scalar: int) -> List[Share]:
        """份额向量缩放"""
        return [s * scalar for s in vec]
    
    @staticmethod
    def scale_bigint_vec(vec: List[int], scalar: int) -> List[int]:
        """整数向量缩放"""
        return [(v * scalar) % Params.P for v in vec]
    
    @staticmethod
    def dot_product_bigint(vec1: List[int], vec2: List[int]) -> int:
        """整数向量点积"""
        if len(vec1) != len(vec2):
            raise ValueError("Vectors must have the same length")
        result = 0
        for v1, v2 in zip(vec1, vec2):
            result = (result + v1 * v2) % Params.P
        return result
    
    @staticmethod
    def dot_product_shares_bigint(shares: List[Share], bigints: List[int]) -> Share:
        """份额向量与整数向量点积"""
        if len(shares) != len(bigints):
            raise ValueError("Vectors must have the same length")
        if not shares:
            raise ValueError("Vectors cannot be empty")
        
        party_id = shares[0].party_id
        result = 0
        for s, b in zip(shares, bigints):
            result = (result + s.shr * b) % Params.P
        return Share(party_id, result)
    
    @staticmethod
    def sum_shares(shares: List[Share]) -> Share:
        """份额向量求和"""
        if not shares:
            raise ValueError("Cannot sum empty list")
        party_id = shares[0].party_id
        total = sum(s.shr for s in shares) % Params.P
        return Share(party_id, total)
    
    @staticmethod
    def sum_bigint(values: List[int]) -> int:
        """整数向量求和"""
        return sum(values) % Params.P
    
    @staticmethod
    def do_filter(vec: List, filter_vec: List[bool]):
        """根据布尔过滤器过滤向量"""
        if len(vec) != len(filter_vec):
            raise ValueError("Vector and filter must have the same length")
        return [v for v, f in zip(vec, filter_vec) if f]
    
    @staticmethod
    def dot_product_with_filter(vec1: List[int], vec2: List[int], filter_vec: List[bool]) -> int:
        """带过滤器的点积"""
        if len(vec1) != len(vec2) or len(vec1) != len(filter_vec):
            raise ValueError("All vectors must have the same length")
        result = 0
        for v1, v2, f in zip(vec1, vec2, filter_vec):
            if f:
                result = (result + v1 * v2) % Params.P
        return result
    
    @staticmethod
    def negate_shares_vec(vec: List[Share]) -> List[Share]:
        """份额向量取负"""
        return [Share(s.party_id, (-s.shr) % Params.P) for s in vec]
    
    @staticmethod
    def negate_bigint_vec(vec: List[int]) -> List[int]:
        """整数向量取负"""
        return [(-v) % Params.P for v in vec]
    
    @staticmethod
    def copy_shares_vec(vec: List[Share]) -> List[Share]:
        """复制份额向量"""
        return [s.copy() for s in vec]
    
    @staticmethod
    def copy_bigint_vec(vec: List[int]) -> List[int]:
        """复制整数向量"""
        return vec.copy()
