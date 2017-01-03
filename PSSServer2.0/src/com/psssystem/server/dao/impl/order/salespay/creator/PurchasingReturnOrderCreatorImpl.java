package com.psssystem.server.dao.impl.order.salespay.creator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.psssystem.connection.vo.CommodityItemVO;
import com.psssystem.connection.vo.PurchasingOrderVO;
import com.psssystem.server.dao.factory.impl.StorageInItemDaoFactoryImpl;
import com.psssystem.server.dao.factory.impl.StorageInReturnItemDaoFactoryImpl;
import com.psssystem.server.dao.factory.inf.ItemFactory;
import com.psssystem.server.dao.inf.order.general.salespay.creator.SalesPayCreator;
import com.psssystem.server.util.DBUtils;

public class PurchasingReturnOrderCreatorImpl extends SalesPayCreator<PurchasingOrderVO> {
	private static PurchasingReturnOrderCreatorImpl INSTANCE=new PurchasingReturnOrderCreatorImpl();
	private ItemFactory factory=new StorageInReturnItemDaoFactoryImpl();
	public PurchasingReturnOrderCreatorImpl(){
		this.itemDao=factory.getInstance();
	}
	@Override
	protected String[] getOrderInfo() {
		return new String[]{"purchasingreturnorder","JHTHD"};
	}

	@Override
	protected boolean updateDB(String id, PurchasingOrderVO order) {
		Connection conn=DBUtils.getConnection();
		PreparedStatement stat=null;
		String sql="insert into purchasingreturnorder(id,customer_id,user_id,salesman,remarks,sum,createddate)values(?,?,?,?,?,?,?)";
		try {
			stat=conn.prepareStatement(sql);
			stat.setString(1, id);
			stat.setInt(2, order.getCustomerID());
			stat.setInt(3, order.getUserID());
			stat.setString(4, order.getSalesman());
			stat.setString(5, order.getRemarks());
			stat.setInt(6, order.getSum());
			stat.setString(7, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			if(stat.executeUpdate()!=1){
					DBUtils.closeConnection(conn);
					DBUtils.closeStateMent(stat);
				return false;
			};
		} catch (SQLException e) {
			e.printStackTrace();
				DBUtils.closeConnection(conn);
				DBUtils.closeStateMent(stat);
			return false;
		}finally{
			DBUtils.closeConnection(conn);
			DBUtils.closeStateMent(stat);
		}
		
		for(CommodityItemVO vo:order.getStorageList()){
			vo=new CommodityItemVO.Builder().copyProperties(vo).operationID(id).build();
			if(!itemDao.addItem(vo)){
				deleteLastAdded(id);
				return false;
			}
		}	
		return true;
	}

	private void deleteLastAdded(String id) {
		Connection conn=DBUtils.getConnection();
		PreparedStatement stat=null;
		String sql="delete from purchasingreturnorder where id=?";
		try {
			stat=conn.prepareStatement(sql);
			stat.setString(1, id);
			stat.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	public static SalesPayCreator getInstance() {
		return INSTANCE;
	}
}
