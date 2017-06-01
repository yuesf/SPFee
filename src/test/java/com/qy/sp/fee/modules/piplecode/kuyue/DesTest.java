package com.qy.sp.fee.modules.piplecode.kuyue;

import com.ffcs.cryto.Cryto;

public class DesTest {
	public static void main(String[] args) {
		// 加密密钥
		String key = "B97FED4E9994E33353F2A65A063DFAA8A31428E11BD7AE59";
		// 待加密字符串
		String source = "order0201402111536470028918108304545131000B2000000B000A8001CZ14021121481207";
		// 调用方法加密
		String targetSource = Cryto.encryptBase643DES(source, key);
		// 输出加密后字符串
		System.out.println(targetSource);
		// 输出加密后字符串为：
		// 0CEzbcvdu/qiPgNBu7wyeRHCy3xL+iXOEf0mlFvNm01UxjeISLFt4VBiCSTd6f1G6G0WEiApfpIk5nC38UOpZbTM3/3qws55+vHYDKxXtMM=
	}

}
