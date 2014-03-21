package com.jinnova.smartpad.partner;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;

import com.jinnova.smartpad.CachedPagingList;
import com.jinnova.smartpad.IPagingList;
import com.jinnova.smartpad.PageMemberMate;
import com.jinnova.smartpad.db.PromotionDao;

public class Operation implements IOperation {
	
	public static final String STORE_MAIN_ID = "BRANCH";
	
	private final String branchId;
	
	private String operationId;
	
	private boolean persisted;
	
	private String name;

	private final Schedule openHours = new Schedule();
	
	private String phone;
	
	private String email;

	private final Catalog rootCatalog;
	
	private final CachedPagingList<IPromotion> promotions;

	private long gpsLon;

	private long gpsLat;
	
	/*private String numberStreet;
	
	private String ward;
	
	private String district;
	
	private String city;*/
	
	private String addressLines;
	
	/**
	 * member cards
	 */
	private final LinkedList<String> memberLevels = new LinkedList<String>();
	
	private Boolean memberNameRequired = false;
	
	private Boolean memberAddressRequired = false;
	
	private Boolean memberPhoneRequired = false;
	
	private Boolean memberEmailRequired = false;
	
	private Boolean memberOfferedFree = false;
	
	private Integer memberOfferedFreeLevel;
	
	private String memberOfferedSurvey;
	
	private Integer memberOfferedSurveyLevel;
	
	public Operation(String branchId, /*String storeId,*/ boolean persisted) {
		this.branchId = branchId;
		//this.storeId = storeId;
		this.persisted = persisted;
		this.rootCatalog = new Catalog(this.branchId, this.branchId, Catalog.CATALOG_ID_ROOT);
		Comparator<IPromotion> memberComparator = new Comparator<IPromotion>() {
			
			@Override
			public int compare(IPromotion o1, IPromotion o2) {
				return o1.getCreationDate().compareTo(o2.getCreationDate());
			}
		};
		PageMemberMate<IPromotion> mate = new PageMemberMate<IPromotion>() {

			@Override
			public IPromotion newMemberInstance() {
				return new Promotion(null, Operation.this.operationId);
			}

			@Override
			public boolean isPersisted(IPromotion member) {
				return ((Promotion) member).isPersisted();
			}

			@Override
			public LinkedList<IPromotion> load(int offset, int pageSize) throws SQLException {
				return new PromotionDao().load(Operation.this.operationId, offset, pageSize);
			}

			@Override
			public void insert(IPromotion t) throws SQLException {
				if (t.getName().getName() == null || "".equals(t.getName().getName())) {
					throw new RuntimeException("Promotion name is missing");
				}
				Date now = new Date();
				((Promotion) t).setCreationDate(now);
				((Promotion) t).setLastUpdate(now);
				String newId = SmartpadCommon.md5(Operation.this.branchId + Operation.this.operationId + t.getName().getName()); 
				new PromotionDao().insert(newId, operationId, Operation.this.branchId, t);
			}

			@Override
			public void update(IPromotion t) throws SQLException {
				Date now = new Date();
				((Promotion) t).setLastUpdate(now);
				new PromotionDao().update(((Promotion) t).getPromotionId(), t);
			}

			@Override
			public void delete(IPromotion t) throws SQLException {
				new PromotionDao().delete(((Promotion) t).getPromotionId(), t);
			}};
		this.promotions = new CachedPagingList<IPromotion>(mate, memberComparator, new IPromotion[0]);
	}
	
	boolean checkBranch(String branchId) {
		return this.branchId.equals(branchId);
	}
	
	String getOperationId() {
		return this.operationId;
	}
	
	public void setOperationId(String operationId) {
		this.operationId = operationId;
	}
	
	boolean isPersisted() {
		return this.persisted;
	}
	
	void setPersisted(boolean b) {
		this.persisted = b;
	}

	@Override
	public void setPromotionPageSize(int pageSize) {
		promotions.setPageSize(pageSize);
	}

	@Override
	public IPagingList<IPromotion> getPromotionPagingList() {
		return promotions;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ICatalog getRootCatalog() {
		return rootCatalog;
	}

	@Override
	public Schedule getOpenHours() {
		return openHours;
	}

	@Override
	public long getGpsLon() {
		return gpsLon;
	}

	@Override
	public void setGpsLon(long gpsLon) {
		this.gpsLon = gpsLon;
	}

	@Override
	public long getGpsLat() {
		return gpsLat;
	}

	@Override
	public void setGpsLat(long gpsLat) {
		this.gpsLat = gpsLat;
	}

	@Override
	public String getAddressLines() {
		return addressLines;
	}

	@Override
	public void setAddressLines(String addressLines) {
		this.addressLines = addressLines;
	}

	@Override
	public String getPhone() {
		return phone;
	}

	@Override
	public void setPhone(String phone) {
		this.phone = phone;
	}

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String[] getMemberLevels() {
		return memberLevels.toArray(new String[memberLevels.size()]);
	}

	@Override
	public void setMemberLevels(String[] levels) {
		StringArrayUtils.load(this.memberLevels, levels);
	}

	@Override
	public Boolean isMemberNameRequired() {
		return memberNameRequired;
	}

	@Override
	public void setMemberNameRequired(boolean memberNameRequired) {
		this.memberNameRequired = memberNameRequired;
	}

	@Override
	public Boolean isMemberAddressRequired() {
		return memberAddressRequired;
	}

	@Override
	public void setMemberAddressRequired(boolean memberAddressRequired) {
		this.memberAddressRequired = memberAddressRequired;
	}

	@Override
	public Boolean isMemberPhoneRequired() {
		return memberPhoneRequired;
	}

	@Override
	public void setMemberPhoneRequired(boolean memberPhoneRequired) {
		this.memberPhoneRequired = memberPhoneRequired;
	}

	@Override
	public Boolean isMemberEmailRequired() {
		return memberEmailRequired;
	}

	@Override
	public void setMemberEmailRequired(boolean memberEmailRequired) {
		this.memberEmailRequired = memberEmailRequired;
	}

	@Override
	public Boolean isMemberOfferedFree() {
		return memberOfferedFree;
	}

	@Override
	public void setMemberOfferedFree(boolean memberOfferedFree) {
		this.memberOfferedFree = memberOfferedFree;
	}

	@Override
	public Integer getMemberOfferedFreeLevel() {
		return memberOfferedFreeLevel;
	}

	@Override
	public void setMemberOfferedFreeLevel(int memberOfferedFreeLevel) {
		this.memberOfferedFreeLevel = memberOfferedFreeLevel;
	}

	@Override
	public String getMemberOfferedSurvey() {
		return memberOfferedSurvey;
	}

	@Override
	public void setMemberOfferedSurvey(String memberOfferedSurvey) {
		this.memberOfferedSurvey = memberOfferedSurvey;
	}

	@Override
	public Integer getMemberOfferedSurveyLevel() {
		return memberOfferedSurveyLevel;
	}

	@Override
	public void setMemberOfferedSurveyLevel(int memberOfferedSurveyLevel) {
		this.memberOfferedSurveyLevel = memberOfferedSurveyLevel;
	}

}
