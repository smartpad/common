import java.sql.SQLException;
import java.util.Iterator;

import com.jinnova.smartpad.batch.IBatchManager;
import com.jinnova.smartpad.db.OperationDao;
import com.jinnova.smartpad.partner.ICatalog;
import com.jinnova.smartpad.partner.ICatalogItem;
import com.jinnova.smartpad.partner.IOperation;
import com.jinnova.smartpad.partner.IPromotion;


public class BatchManager implements IBatchManager {

	@Override
	public Iterator<IOperation> operationIterator() throws SQLException {
		return new OperationDao().operationIterator();
	}

	@Override
	public Iterator<ICatalog> catalogIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<ICatalogItem> catalogItemIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<IPromotion> promotionIterator() {
		// TODO Auto-generated method stub
		return null;
	}

}
