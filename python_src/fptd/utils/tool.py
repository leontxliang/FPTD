"""
通用工具类
"""

import secrets
import math
from typing import List, Tuple
from ..params import Params
from ..share import Share
from ..sharing.shamir_sharing import ShamirSharing


class Tool:
    """通用工具方法"""
    
    @staticmethod
    def get_rand(bit_length: int = None) -> int:
        """生成随机数"""
        if bit_length:
            return secrets.randbits(bit_length)
        return secrets.randbelow(Params.P)
    
    @staticmethod
    def get_rand_below(upper: int) -> int:
        """生成小于upper的随机数"""
        return secrets.randbelow(upper)
    
    @staticmethod
    def open_shares_to_values(all_shares: List[List[Share]]) -> List[int]:
        """
        从所有参与方的份额恢复明文值
        
        Args:
            all_shares: all_shares[party_idx][value_idx] = Share
            
        Returns:
            恢复的明文值列表
        """
        if not all_shares or not all_shares[0]:
            return []
        
        num_values = len(all_shares[0])
        sharing = ShamirSharing()
        
        values = []
        for v_idx in range(num_values):
            shares = [all_shares[p_idx][v_idx] for p_idx in range(len(all_shares))]
            values.append(sharing.recover(shares))
        
        return values
    
    @staticmethod
    def to_signed(value: int) -> int:
        """将有限域中的值转换为有符号整数"""
        half_p = Params.P // 2
        if value > half_p:
            return value - Params.P
        return value
    
    @staticmethod
    def to_float(value: int) -> float:
        """将定点数转换为浮点数"""
        signed = Tool.to_signed(value)
        return signed / Params.PRECISE_ROUND
    
    @staticmethod
    def to_fixed_point(value: float) -> int:
        """将浮点数转换为定点数"""
        fixed = int(round(value * Params.PRECISE_ROUND))
        return fixed % Params.P
    
    @staticmethod
    def get_accuracy_rmse(predictions: List[float], truths: List[float]) -> float:
        """计算RMSE (均方根误差)"""
        if len(predictions) != len(truths):
            raise ValueError("Predictions and truths must have the same length")
        if not predictions:
            return 0.0
        
        mse = sum((p - t) ** 2 for p, t in zip(predictions, truths)) / len(predictions)
        return math.sqrt(mse)
    
    @staticmethod
    def get_accuracy_mae(predictions: List[float], truths: List[float]) -> float:
        """计算MAE (平均绝对误差)"""
        if len(predictions) != len(truths):
            raise ValueError("Predictions and truths must have the same length")
        if not predictions:
            return 0.0
        
        return sum(abs(p - t) for p, t in zip(predictions, truths)) / len(predictions)
    
    @staticmethod
    def mod_inverse(a: int, p: int = None) -> int:
        """计算模逆元"""
        p = p or Params.P
        return pow(a, -1, p)
    
    @staticmethod
    def mod_pow(base: int, exp: int, mod: int = None) -> int:
        """模幂运算"""
        mod = mod or Params.P
        return pow(base, exp, mod)
    
    @staticmethod
    def floor_div(a: int, b: int) -> int:
        """整数除法 (向下取整)"""
        return a // b
    
    @staticmethod
    def create_zero_shares(party_id: int, size: int) -> List[Share]:
        """创建零份额向量"""
        return [Share(party_id, 0) for _ in range(size)]
    
    @staticmethod
    def create_zero_bigint_vec(size: int) -> List[int]:
        """创建零整数向量"""
        return [0] * size
