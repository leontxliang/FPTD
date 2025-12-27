"""
线性代数工具类 (NumPy 版本)
"""

import numpy as np
from typing import List, Union
from ..params import Params
from ..share import Share


class LinearAlgebra:
    """向量和矩阵运算工具 (使用 NumPy 加速)"""
    
    @staticmethod
    def add_shares_vec(vec1: List[Share], vec2: List[Share]) -> List[Share]:
        """份额向量加法"""
        return [s1 + s2 for s1, s2 in zip(vec1, vec2)]
    
    @staticmethod
    def subtract_shares_vec(vec1: List[Share], vec2: List[Share]) -> List[Share]:
        """份额向量减法"""
        return [s1 - s2 for s1, s2 in zip(vec1, vec2)]
    
    @staticmethod
    def add_vec(vec1: np.ndarray, vec2: np.ndarray, p: int = None) -> np.ndarray:
        """向量加法 (模 p)"""
        p = p or Params.P
        return (vec1 + vec2) % p
    
    @staticmethod
    def subtract_vec(vec1: np.ndarray, vec2: np.ndarray, p: int = None) -> np.ndarray:
        """向量减法 (模 p)"""
        p = p or Params.P
        return (vec1 - vec2) % p
    
    @staticmethod
    def elem_wise_multiply(vec1: np.ndarray, vec2: np.ndarray, p: int = None) -> np.ndarray:
        """逐元素乘法 (模 p)"""
        p = p or Params.P
        return (vec1 * vec2) % p
    
    @staticmethod
    def scale_vec(vec: np.ndarray, scalar: int, p: int = None) -> np.ndarray:
        """向量缩放 (模 p)"""
        p = p or Params.P
        return (vec * scalar) % p
    
    @staticmethod
    def dot_product(vec1: np.ndarray, vec2: np.ndarray, p: int = None) -> int:
        """向量点积 (模 p)"""
        p = p or Params.P
        # 使用 Python int 避免溢出
        return int(np.sum(vec1.astype(object) * vec2.astype(object)) % p)
    
    @staticmethod
    def dot_product_shares_bigint(shares: List[Share], bigints: np.ndarray) -> Share:
        """份额向量与整数向量点积"""
        if not shares:
            raise ValueError("Vectors cannot be empty")
        party_id = shares[0].party_id
        share_vals = np.array([s.shr for s in shares], dtype=object)
        result = int(np.sum(share_vals * bigints.astype(object)) % Params.P)
        return Share(party_id, result)
    
    @staticmethod
    def sum_vec(vec: np.ndarray, p: int = None) -> int:
        """向量求和 (模 p)"""
        p = p or Params.P
        return int(np.sum(vec.astype(object)) % p)
    
    @staticmethod
    def sum_shares(shares: List[Share]) -> Share:
        """份额向量求和"""
        if not shares:
            raise ValueError("Cannot sum empty list")
        party_id = shares[0].party_id
        total = sum(s.shr for s in shares) % Params.P
        return Share(party_id, total)
    
    @staticmethod
    def do_filter(vec: np.ndarray, filter_vec: np.ndarray) -> np.ndarray:
        """根据布尔过滤器过滤向量"""
        return vec[filter_vec]
    
    @staticmethod
    def dot_product_with_filter(vec1: np.ndarray, vec2: np.ndarray, 
                                 filter_vec: np.ndarray, p: int = None) -> int:
        """带过滤器的点积"""
        p = p or Params.P
        filtered1 = vec1[filter_vec].astype(object)
        filtered2 = vec2[filter_vec].astype(object)
        return int(np.sum(filtered1 * filtered2) % p)
    
    @staticmethod
    def negate_vec(vec: np.ndarray, p: int = None) -> np.ndarray:
        """向量取负 (模 p)"""
        p = p or Params.P
        return (-vec) % p
    
    @staticmethod
    def to_numpy(lst: List[int]) -> np.ndarray:
        """将列表转换为 NumPy 数组"""
        return np.array(lst, dtype=object)
    
    @staticmethod
    def to_list(arr: np.ndarray) -> List[int]:
        """将 NumPy 数组转换为列表"""
        return [int(x) for x in arr]
    
    @staticmethod
    def zeros(size: int) -> np.ndarray:
        """创建零向量"""
        return np.zeros(size, dtype=object)
    
    @staticmethod
    def ones(size: int) -> np.ndarray:
        """创建全1向量"""
        return np.ones(size, dtype=object)


# 便捷函数
def np_mod(arr: np.ndarray, p: int = None) -> np.ndarray:
    """对数组取模"""
    p = p or Params.P
    return arr % p
