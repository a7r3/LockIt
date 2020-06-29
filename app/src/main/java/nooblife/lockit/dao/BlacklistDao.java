package nooblife.lockit.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import nooblife.lockit.model.Blacklist;

@Dao
public interface BlacklistDao {

    @Query("SELECT * FROM apps WHERE profileName = 'default'")
    Blacklist getBlacklist();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void createBlacklist(Blacklist blacklist);

    @Query("SELECT isActive FROM apps WHERE profileName = 'default'")
    boolean isServiceActive();

    @Query("UPDATE apps SET isActive = :isServiceRunningNow WHERE profileName = 'default'")
    void setServiceActive(boolean isServiceRunningNow);

}
