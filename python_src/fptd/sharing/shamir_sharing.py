"""
Shamir秘密共享实现
"""

import secrets
from typing import List
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
    
    def get_shares(self, secret: int) -> List[Share]:
        """
        将秘密分割成n个份额
        
        构造 (t-1) 次多项式: f(x) = secret + a1*x + a2*x^2 + ... + a_{t-1}*x^{t-1}
        在点 x=1,2,...,n 处求值生成份额
        
        Args:
            secret: 要分享的秘密值
            
        Returns:
            n个份额的列表
        """
        secret = secret % self.p
        
        # 生成随机系数 a1, a2, ..., a_{t-1}
        coefficients = [secret]
        for _ in range(self.t - 1):
            coefficients.append(secrets.randbelow(self.p))
        
        # 在点 x=1,2,...,n 处求值
        shares = []
        for i in range(1, self.n + 1):
            # 计算 f(i) = secret + a1*i + a2*i^2 + ...
            value = 0
            x_power = 1
            for coeff in coefficients:
                value = (value + coeff * x_power) % self.p
                x_power = (x_power * i) % self.p
            shares.append(Share(i - 1, value))  # party_id从0开始
        
        return shares
    
    def recover(self, shares: List[Share]) -> int:
        """
        从份额中恢复秘密 (拉格朗日插值)
        
        Args:
            shares: 至少t个份额
            
        Returns:
            恢复的秘密值
        """
        if len(shares) < self.t:
            raise ValueError(f"Need at least {self.t} shares, got {len(shares)}")
        
        # 只使用前t个份额
        shares = shares[:self.t]
        
        # 获取x坐标 (party_id + 1)
        x_coords = [share.party_id + 1 for share in shares]
        
        # 拉格朗日插值计算 f(0)
        secret = 0
        for i, share in enumerate(shares):
            xi = x_coords[i]
            
            # 计算拉格朗日基多项式 L_i(0) = Π_{j≠i} (0 - xj) / (xi - xj)
            numerator = 1
            denominator = 1
            for j, xj in enumerate(x_coords):
                if i != j:
                    numerator = (numerator * (-xj)) % self.p
                    denominator = (denominator * (xi - xj)) % self.p
            
            # 计算 L_i(0) = numerator / denominator (mod p)
            lagrange_coeff = (numerator * pow(denominator, -1, self.p)) % self.p
            
            # 累加 share_i * L_i(0)
            secret = (secret + share.shr * lagrange_coeff) % self.p
        
        return secret
    
    def recover_at_point(self, shares: List[Share], point: int) -> int:
        """
        在指定点处进行拉格朗日插值
        
        Args:
            shares: 份额列表
            point: 插值点
            
        Returns:
            插值结果
        """
        if len(shares) < self.t:
            raise ValueError(f"Need at least {self.t} shares, got {len(shares)}")
        
        shares = shares[:self.t]
        x_coords = [share.party_id + 1 for share in shares]
        
        result = 0
        for i, share in enumerate(shares):
            xi = x_coords[i]
            
            numerator = 1
            denominator = 1
            for j, xj in enumerate(x_coords):
                if i != j:
                    numerator = (numerator * (point - xj)) % self.p
                    denominator = (denominator * (xi - xj)) % self.p
            
            lagrange_coeff = (numerator * pow(denominator, -1, self.p)) % self.p
            result = (result + share.shr * lagrange_coeff) % self.p
        
        return result


def generate_shares_for_all_parties(values: List[int], n: int = None, t: int = None) -> List[List[Share]]:
    """
    为多个值生成所有参与方的份额
    
    Args:
        values: 要分享的值列表
        n: 参与方数量
        t: 门限值
        
    Returns:
        shares_per_party[party_idx][value_idx] = Share
    """
    n = n or Params.N
    t = t or Params.T
    sharing = ShamirSharing(n, t)
    
    # shares_per_value[value_idx][party_idx] = Share
    shares_per_value = [sharing.get_shares(v) for v in values]
    
    # 转置为 shares_per_party[party_idx][value_idx] = Share
    shares_per_party = []
    for party_idx in range(n):
        party_shares = [shares_per_value[v_idx][party_idx] for v_idx in range(len(values))]
        shares_per_party.append(party_shares)
    
    return shares_per_party
