package ro.atrifan.persistence.dao;

import ro.atrifan.persistence.entities.DashBoard;

/**
 * Created by alexandru.trifan on 24.04.2016.
 */
public interface DashBoardDao extends AbstractDao<DashBoard>{

    public DashBoard findByGroupName(String group);
}
