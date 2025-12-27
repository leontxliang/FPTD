"""
Shamir 秘密共享实现 (NumPy 版本)
"""

import secrets
import numpy as np
from typing import List, Union
from ..params import Params
from ..share import Share


class ShamirSharing:
    """Shamir (t,n) 门限秘密共享方案"""
    
    def __init__(self, n: int = None, t: int = None, p: int = None):
        """
        Args:
            n: 参与方数量
            t: 门限值 (恢复秘密所需的最小份额数)
            p: 有限域素数
        """
        self.n = n or Params.N
        self.t = t or Params.T
        self.p = p or Params.P
        
        # 预计算拉格朗日系数 (用于恢复秘密)
        self._lagrange_coeffs = None
    
    def get_shares(self, secret: int) -> List[Share]:
        """
        将秘密分割成 n 个份额
        
        构造 (t-1) 次多项式: f(x) = secret + a1*x + a2*x^2 + ... + a_{t-1}*x^{t-1}
        在点 x=1,2,...,n 处求值生成份额
        """
        secret = int(secret) % self.p
        
        # 生成随机系数
        coefficients = np.array(
            [secret] + [secrets.randbelow(self.p) for _ in range(self.t - 1)],
            dtype=object
        )
        
        # 在点 x=1,2,...,n 处求值
        shares = []
        for i in range(1, self.n + 1):
            # 使用霍纳法则计算多项式值
            value = coefficients[-1]
            for coeff in coefficients[-2::-1]:
                value = (value * i + coeff) % self.p
            shares.append(Share(i - 1, int(value)))
        
        return shares
    
    def get_shares_batch(self, secrets_arr: np.ndarray) -> np.ndarray:
        """
        批量生成份额
        
        Args:
            secrets_arr: 秘密值数组
            
        Returns:
            shares_matrix: shape (n, len(secrets_arr)), shares_matrix[party_idx][secret_idx]
        """
        num_secrets = len(secrets_arr)
        secrets_arr = secrets_arr.astype(object) % self.p
        
        # 生成随机系数矩阵 (t-1, num_secrets)
        rand_coeffs = np.array(
            [[secrets.randbelow(self.p) for _ in range(num_secrets)] for _ in range(self.t - 1)],
            dtype=object
        )
        
        # 系数矩阵 (t, num_secrets), 第一行是秘密
        coefficients = np.vstack([secrets_arr.reshape(1, -1), rand_coeffs])
        
        # 计算每个参与方的份额
        shares_matrix = np.zeros((self.n, num_secrets), dtype=object)
        for i in range(1, self.n + 1):
            # 霍纳法则
            value = coefficients[-1].copy()
            for coeff in coefficients[-2::-1]:
                value = (value * i + coeff) % self.p
            shares_matrix[i - 1] = value
        
        return shares_matrix
    
    def recover(self, shares: List[Share]) -> int:
        """从份额中恢复秘密 (拉格朗日插值)"""
        if len(shares) < self.t:
            raise ValueError(f"Need at least {self.t} shares, got {len(shares)}")
        
        # 只使用前 t 个份额
        shares = shares[:self.t]
        x_coords = np.array([share.party_id + 1 for share in shares], dtype=object)
        y_coords = np.array([share.shr for share in shares], dtype=object)
        
        # 拉格朗日插值计算 f(0)
        secret = 0
        for i in range(self.t):
            xi = x_coords[i]
            
            # 计算拉格朗日基多项式 L_i(0)
            numerator = 1
            denominator = 1
            for j in range(self.t):
                if i != j:
                    xj = x_coords[j]
                    numerator = (numerator * (-xj)) % self.p
                    denominator = (denominator * (xi - xj)) % self.p
            
            lagrange_coeff = (numerator * pow(int(denominator), -1, self.p)) % self.p
            secret = (secret + y_coords[i] * lagrange_coeff) % self.p
        
        return int(secret)
    
    def recover_batch(self, shares_matrix: np.ndarray) -> np.ndarray:
        """
        批量恢复秘密
        
        Args:
            shares_matrix: shape (n, num_secrets)
            
        Returns:
            secrets_arr: 恢复的秘密数组
        """
        # 只使用前 t 个份额
        shares_matrix = shares_matrix[:self.t]
        x_coords = np.arange(1, self.t + 1, dtype=object)
        
        # 预计算拉格朗日系数
        lagrange_coeffs = np.zeros(self.t, dtype=object)
        for i in range(self.t):
            xi = x_coords[i]
            numerator = 1
            denominator = 1
            for j in range(self.t):
                if i != j:
                    xj = x_coords[j]
                    numerator = (numerator * (-xj)) % self.p
                    denominator = (denominator * (xi - xj)) % self.p
            lagrange_coeffs[i] = (numerator * pow(int(denominator), -1, self.p)) % self.p
        
        # 计算秘密
        secrets_arr = np.zeros(shares_matrix.shape[1], dtype=object)
        for i in range(self.t):
            secrets_arr = (secrets_arr + shares_matrix[i] * lagrange_coeffs[i]) % self.p
        
        return secrets_arr


def generate_shares_for_all_parties(values: Union[List[int], np.ndarray], 
                                     n: int = None, t: int = None) -> List[List[Share]]:
    """
    为多个值生成所有参与方的份额
    
    Returns:
        shares_per_party[party_idx][value_idx] = Share
    """
    n = n or Params.N
    t = t or Params.T
    sharing = ShamirSharing(n, t)
    
    values = np.asarray(values, dtype=object)
    shares_matrix = sharing.get_shares_batch(values)
    
    # 转换为 Share 对象列表
    shares_per_party = []
    for party_idx in range(n):
        party_shares = [Share(party_idx, int(v)) for v in shares_matrix[party_idx]]
        shares_per_party.append(party_shares)
    
    return shares_per_party
