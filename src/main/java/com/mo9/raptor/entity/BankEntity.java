package com.mo9.raptor.entity;

import javax.persistence.*;
import java.util.Date;

/**
 * 银行卡四要素验证表
 * @author xtgu
 *
 */
@Entity
@Table(name = "t_raptor_bank")
public class BankEntity {


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id ;

	/**
	 * 用户编号
	 */
	@Column(name = "user_code")
	private String userCode;

	/**
	 * 银行卡
	 */
	@Column(name = "bank_no")
	private String bankNo ;

	/**
	 * 银行名称
	 */
	@Column(name = "bank_name")
	private String bankName ;

	/**
	 * 身份证
	 */
	@Column(name = "card_id")
	private String cardId ;

	/**
	 * 银行卡用户名
	 */
	@Column(name = "user_name")
	private String userName  ;

	/**
	 * 手机号
	 */
	@Column(name = "mobile")
	private String mobile ;

	/**
	 * 创建时间
	 */
	@Column(name = "create_time")
	private Long createTime ;

	/**
	 * 修改时间
	 */
	@Column(name = "update_time")
	private Long updateTime ;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBankNo() {
		return bankNo;
	}

	public void setBankNo(String bankNo) {
		this.bankNo = bankNo;
	}

	public String getCardId() {
		return cardId;
	}

	public void setCardId(String cardId) {
		this.cardId = cardId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}


	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	public Long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}


	public String getUserCode() {
		return userCode;
	}

	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}

}
