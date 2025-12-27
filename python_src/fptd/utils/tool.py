"""
通用工具类 (NumPy 版本)
"""

import secrets
import numpy as np
from typing import List, Tuple, Union
from ..params import Params
from ..share import Share


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
        """生成小于 upper 的随机数"""
        return secrets.randbelow(upper)
    
    @staticmethod
    def get_rand_array(size: int, upper: int = None) -> np.ndarray:
        """生成随机数数组"""
        upper = upper or Params.P
        # 使用 numpy 生成大随机数
        return np.array([secrets.randbelow(upper) for _ in range(size)], dtype=object)
    
    @staticmethod
    def open_shares_to_values(all_shares: List[List[Share]]) -> np.ndarray:
        """从所有参与方的份额恢复明文值"""
        from ..sharing.shamir_sharing import ShamirSharing
        
        if not all_shares or not all_shares[0]:
            return np.array([], dtype=object)
        
        num_values = len(all_shares[0])
        sharing = ShamirSharing()
        
        values = np.zeros(num_values, dtype=object)
        for v_idx in range(num_values):
            shares = [all_shares[p_idx][v_idx] for p_idx in range(len(all_shares))]
            values[v_idx] = sharing.recover(shares)
        
        return values
    
    @staticmethod
    def to_signed(value: Union[int, np.ndarray]) -> Union[int, np.ndarray]:
        """将有限域中的值转换为有符号整数"""
        half_p = Params.P // 2
        if isinstance(value, np.ndarray):
            result = value.copy()
            mask = result > half_p
            result[mask] = result[mask] - Params.P
            return result
        else:
            return value - Params.P if value > half_p else value
    
    @staticmethod
    def to_float(value: Union[int, np.ndarray]) -> Union[float, np.ndarray]:
        """将定点数转换为浮点数"""
        signed = Tool.to_signed(value)
        if isinstance(signed, np.ndarray):
            return signed.astype(float) / Params.PRECISE_ROUND
        return signed / Params.PRECISE_ROUND
    
    @staticmethod
    def to_fixed_point(value: Union[float, np.ndarray]) -> Union[int, np.ndarray]:
        """将浮点数转换为定点数"""
        if isinstance(value, np.ndarray):
            fixed = np.round(value * Params.PRECISE_ROUND).astype(object)
            return fixed % Params.P
        fixed = int(round(value * Params.PRECISE_ROUND))
        return fixed % Params.P
    
    @staticmethod
    def get_accuracy_rmse(predictions: np.ndarray, truths: np.ndarray) -> float:
        """计算 RMSE (均方根误差)"""
        predictions = np.asarray(predictions, dtype=float)
        truths = np.asarray(truths, dtype=float)
        return float(np.sqrt(np.mean((predictions - truths) ** 2)))
    
    @staticmethod
    def get_accuracy_mae(predictions: np.ndarray, truths: np.ndarray) -> float:
        """计算 MAE (平均绝对误差)"""
        predictions = np.asarray(predictions, dtype=float)
        truths = np.asarray(truths, dtype=float)
        return float(np.mean(np.abs(predictions - truths)))
    
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
    def create_zero_shares(party_id: int, size: int) -> List[Share]:
        """创建零份额向量"""
        return [Share(party_id, 0) for _ in range(size)]
    
    @staticmethod
    def create_zeros(size: int) -> np.ndarray:
        """创建零向量"""
        return np.zeros(size, dtype=object)
    
    @staticmethod
    def create_ones(size: int) -> np.ndarray:
        """创建全1向量"""
        return np.ones(size, dtype=object)
