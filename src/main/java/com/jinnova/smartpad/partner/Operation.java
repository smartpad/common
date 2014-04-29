package com.jinnova.smartpad.partner;

import static com.jinnova.smartpad.partner.IDetailManager.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.LinkedList;

import com.google.gson.JsonObject;
import com.jinnova.smartpad.CachedPagingList;
import com.jinnova.smartpad.Feed;
import com.jinnova.smartpad.IName;
import com.jinnova.smartpad.IPagingList;
import com.jinnova.smartpad.Name;
import com.jinnova.smartpad.PageEntrySupport;
import com.jinnova.smartpad.RecordInfo;
import com.jinnova.smartpad.db.MemberDao;
import com.jinnova.smartpad.db.PromotionDao;
import com.jinnova.smartpad.member.IMember;
import com.jinnova.smartpad.member.Member;

/**
 * An operation could be a branch or a store
 * 
 * NOTE: For a business that has a single store, we have to represent a branch with zero store. Otherwise, when searching similar branches,
 * we can't find these businesses.
 * 
 * @author HuyBA
 *
 */
public class Operation implements IOperation, Feed {
	
	private final String branchId;
	
	private String storeId;
	
	private final RecordInfo recordInfo = new RecordInfo();
	
	private final Name name = new Name();
	
	public final GPSInfo gps = new GPSInfo();

	private final Schedule openHours = new Schedule();
	
	private String phone;
	
	private String email;
	
	//static final String CATALOG_ID_OPERROOT = "OPER_ROOT";
	//private final Catalog rootCatalog = new Catalog(this.branchId, this.branchId, CATALOG_ID_OPERROOT);
	private String systemCatalogId;
	private Catalog rootCatalog;
	
	//private String systemCatalogId;
	
	private CachedPagingList<IPromotion, IPromotionSort> promotions;
	
	/*private String numberStreet;
	
	private String ward;
	
	private String district;
	
	private String city;*/
	
	private String addressLines;
	
	/**
	 * member cards
	 */
	private final LinkedList<String> memberLevels = new LinkedList<String>();
	
	private final CachedPagingList<IMember, IMemberSort> memberPagingList;
	
	//private Boolean memberNameRequired = false;
	
	//private Boolean memberAddressRequired = false;
	
	//private Boolean memberPhoneRequired = false;
	
	//private Boolean memberEmailRequired = false;
	
	//private Boolean memberOfferedFree = false;
	
	//private Integer memberOfferedFreeLevel;
	
	//private String memberOfferedSurvey;
	
	//private Integer memberOfferedSurveyLevel;

	public Operation(String storeId, String branchId, String systemCatalogId, BigDecimal gpsLon, BigDecimal gpsLat, String gpsInherit, boolean branch) {
		this.storeId = storeId;
		this.branchId = branchId;
		this.gps.setLongitude(gpsLon);
		this.gps.setLatitude(gpsLat);
		this.gps.setInheritFrom(gpsInherit);
		
		String rootCatInherit;
		if (branch) {
			rootCatInherit = GPSInfo.INHERIT_BRANCH;
		} else {
			rootCatInherit = GPSInfo.INHERIT_STORE;
		}
		
		this.systemCatalogId = systemCatalogId;
		this.rootCatalog = new Catalog(this.branchId, this.storeId, this.storeId, this.storeId, this.systemCatalogId);
		this.rootCatalog.gps.inherit(this.gps, rootCatInherit);
		/*this.rootCatalog.gps.setLongitude(gpsLon);
		this.rootCatalog.gps.setLatitude(gpsLat);
		this.rootCatalog.gps.setInheritFrom(gpsInherit);*/
		
		this.promotions = createPromotionPagingList(branchId, storeId, this.systemCatalogId, gps);
		this.memberPagingList = createMemberPagingList();
	}
	
	public static CachedPagingList<IPromotion, IPromotionSort> createPromotionPagingList(
			final String branchId, final String storeId, final String syscatId, final GPSInfo gps) {
		
		@SuppressWarnings({ "unchecked" })
		final Comparator<IPromotion>[] promoComparators = new Comparator[IPromotionSort.values().length];
		promoComparators[IPromotionSort.creation.ordinal()] = new Comparator<IPromotion>() {
			@Override
			public int compare(IPromotion o1, IPromotion o2) {
				return o1.getRecordInfo().getCreateDate().compareTo(o2.getRecordInfo().getCreateDate());
			}
		};
		promoComparators[IPromotionSort.lastUpdate.ordinal()] = new Comparator<IPromotion>() {
			@Override
			public int compare(IPromotion o1, IPromotion o2) {
				return o1.getRecordInfo().getUpdateDate().compareTo(o2.getRecordInfo().getUpdateDate());
			}
		};
		promoComparators[IPromotionSort.name.ordinal()] = new Comparator<IPromotion>() {
			@Override
			public int compare(IPromotion o1, IPromotion o2) {
				return o1.getName().getName().compareTo(o2.getName().getName());
			}
		};
		
		PageEntrySupport<IPromotion, IPromotionSort> promoMate = new PageEntrySupport<IPromotion, IPromotionSort>() {

			@Override
			public IPromotion newEntryInstance(IUser authorizedUser) {
				String gpsInherit;
				if (branchId.equals(storeId)) {
					gpsInherit = GPSInfo.INHERIT_BRANCH;
				} else {
					gpsInherit = GPSInfo.INHERIT_STORE;
				}
				Promotion promo = new Promotion(null, branchId, storeId, syscatId);
				promo.gps.inherit(gps, gpsInherit);
				return promo;
			}

			@Override
			public boolean isPersisted(IPromotion member) {
				return ((Promotion) member).isPersisted();
			}

			@Override
			public int count(IUser authorizedUser) throws SQLException {
				return new PromotionDao().count(storeId);
			}

			@Override
			public LinkedList<IPromotion> load(IUser authorizedUser, int offset, int pageSize, IPromotionSort sortField, boolean ascending) throws SQLException {
				return new PromotionDao().load(storeId, offset, pageSize, sortField, ascending);
			}

			@Override
			public void insert(IUser authorizedUser, IPromotion t) throws SQLException {
				if (t.getName().getName() == null || "".equals(t.getName().getName())) {
					throw new RuntimeException("Promotion name is missing");
				}
				String newId = SmartpadCommon.md5(branchId + storeId + t.getName().getName()); 
				new PromotionDao().insert(newId, branchId, storeId, syscatId, (Promotion) t);
			}

			@Override
			public void update(IUser authorizedUser, IPromotion t) throws SQLException {
				new PromotionDao().update(((Promotion) t).getId(), (Promotion) t);
			}

			@Override
			public void delete(IUser authorizedUser, IPromotion t) throws SQLException {
				new PromotionDao().delete(((Promotion) t).getId(), t);
			}};
		return new CachedPagingList<IPromotion, IPromotionSort>(promoMate, promoComparators, IPromotionSort.creation, new IPromotion[0]);
	}
	
	private CachedPagingList<IMember, IMemberSort> createMemberPagingList() {
		
		@SuppressWarnings("unchecked")
		Comparator<IMember>[] memberComparators = new Comparator[IMemberSort.values().length];
		PageEntrySupport<IMember, IMemberSort> memberMate = new PageEntrySupport<IMember, IMemberSort>() {
			
			@Override
			public void update(IUser authorizedUser, IMember member) throws SQLException {
				new MemberDao().update(((Member) member).getId(), member);
			}
			
			@Override
			public IMember newEntryInstance(IUser authorizedUser) {
				return new Member(null);
			}
			
			@Override
			public int count(IUser authorizedUser) throws SQLException {
				return new MemberDao().count(Operation.this.branchId, Operation.this.storeId);
			}
			
			@Override
			public LinkedList<IMember> load(IUser authorizedUser, int offset,
					int pageSize, IMemberSort sortField, boolean ascending) throws SQLException {
				
				return new MemberDao().load(Operation.this.branchId, Operation.this.storeId, offset, pageSize, sortField, ascending);
			}
			
			@Override
			public boolean isPersisted(IMember member) {
				return ((Member) member).getId() != null;
			}
			
			@Override
			public void insert(IUser authorizedUser, IMember newMember) throws SQLException {
				String name = newMember.getName();
				if (name == null || "".equals(name.trim())) {
					throw new RuntimeException("Member name is not set");
				}
				String id = ((Member) newMember).generateId();
				new MemberDao().insert(id, Operation.this.storeId, Operation.this.branchId, newMember);
				((Member) newMember).setId(id);
			}
			
			@Override
			public void delete(IUser authorizedUser, IMember member) throws SQLException {
				new MemberDao().delete(((Member) member).getId(), member);
			}
		};
		return new CachedPagingList<IMember, IMemberSort>(memberMate, memberComparators, IMemberSort.Creation, new IMember[0]);
	}
	
	public String getBranchId() {
		return this.branchId;
	}
	
	@Override
	public RecordInfo getRecordInfo() {
		return this.recordInfo;
	}

	@Override
	public IGPSInfo getGps() {
		return gps;
	}
	
	boolean checkBranch(String branchId) {
		return this.branchId.equals(branchId);
	}
	
	@Override
	public String getId() {
		return this.storeId;
	}
	
	public void setId(String operationId) {
		this.storeId = operationId;
		this.rootCatalog = new Catalog(this.branchId, this.storeId, this.storeId, this.storeId, this.systemCatalogId);
		createMemberPagingList();
	}

	@Override
	public IPagingList<IPromotion, IPromotionSort> getPromotionPagingList() {
		return promotions;
	}

	@Override
	public IName getName() {
		return name;
	}

	@Override
	public ICatalog getRootCatalog() {
		return rootCatalog;
	}
	
	public String getSyscatId() {
		return rootCatalog.getSystemCatalogId();
	}
	
	@Override
	public void setSystemCatalogId(String syscatId) {
		this.systemCatalogId = syscatId;
		this.rootCatalog.setSystemCatalogId(syscatId);
		this.promotions = createPromotionPagingList(branchId, storeId, this.systemCatalogId, gps);
	}

	/*@Override
	public String getSystemCatalogId() {
		return this.rootCatalog.getSystemCatalogId();
	}*/

	/*@Override
	public void setSystemCatalogId(String systemCatalogId) {
		this.rootCatalog.setSystemCatalogId(systemCatalogId);
	}*/

	@Override
	public Schedule getOpenHours() {
		return openHours;
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
	public IPagingList<IMember, IMemberSort> getMemberPagingList() {
		return memberPagingList;
	}
	
	public JsonObject generateFeedJson() {
		JsonObject json = new JsonObject();
		json.addProperty(FIELD_ID, this.storeId);
		if (this.storeId.equals(this.branchId)) {
			json.addProperty(FIELD_TYPE, IDetailManager.TYPENAME_BRANCH);
		} else {
			json.addProperty(FIELD_TYPE, IDetailManager.TYPENAME_STORE);
		}
		//json.addProperty(FIELD_SYSCATID, this.systemCatalogId);
		json.addProperty(FIELD_NAME, this.name.getName());
		return json;
	}

	/*@Override
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
	}*/

}
