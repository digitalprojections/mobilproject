package furqon.io.github.mobilproject;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Timestamp;

@Entity (tableName = "messages")
public class MessageTable {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String message_title;
    public String message_body;
    public String date_time;
    public int message_read;
    public int category;
    public Boolean trashed;

    public void setId(int id) {
        this.id = id;
    }

    public MessageTable(String message_title, String message_body, String date_time) {
        this.message_title = message_title;
        this.message_body = message_body;
        this.date_time = date_time;
        this.message_read = 0;
        this.category = 0;
        this.trashed = false;
    }
}
