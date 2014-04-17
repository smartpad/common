import java.sql.SQLException;
import java.util.Iterator;

import com.jinnova.smartpad.batch.IBatchManager;
import com.jinnova.smartpad.partner.ICatalog;
import com.jinnova.smartpad.partner.ICatalogItem;
import com.jinnova.smartpad.partner.IOperation;
import com.jinnova.smartpad.partner.IPromotion;


public class BatchManager implements IBatchManager {

	@Override
	public Iterator<IOperation> operationIterator() throws SQLException {
		return null; //new OperationDao().operationIterator();
	}

	@Override
	public Iterator<ICatalog> catalogIterator() {
		return null;
	}

	@Override
	public Iterator<ICatalogItem> catalogItemIterator() {
		return null;
	}

	@Override
	public Iterator<IPromotion> promotionIterator() {
		return null;
	}

}
