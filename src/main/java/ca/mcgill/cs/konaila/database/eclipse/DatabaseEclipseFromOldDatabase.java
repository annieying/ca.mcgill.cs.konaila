package ca.mcgill.cs.konaila.database.eclipse;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import ca.mcgill.cs.konaila.database.Database;

public class DatabaseEclipseFromOldDatabase {  

    private static final String dir = "eclipse-database/";
    private static final String DB = "jdbc:sqlite:" + dir + "annotations.sqlite";
        
    public static void main(String[] args) throws Exception {
        
        Map<Integer,String> sidToCode = getCodeFragments();
                
        for( Entry<Integer,String> e : sidToCode.entrySet()) {
            int sid = e.getKey();
            String code = e.getValue();
            String filename = dir + "eclipse-faq/E" + ( sid<10? ("00"+sid) : ( sid<100? ("0"+sid) : (""+sid))) + ".java";
            //                  String filename = dir + "eclipse-faq/E" + sid  + ".java";
            FileUtils.write(new File(filename), code);
        }               
    }
        
    public static Map<Integer,String> getCodeFragments() throws SQLException, IOException {
    	Connection conn = Database.getInstance().getConnection();
        Map<Integer, String> sidToCode = getCodeFragments(conn);
        return sidToCode;
    }
        
    private static Collection<Integer> getSids(Connection conn) throws SQLException, IOException {

        Collection<Integer> sids = new ArrayList<Integer>();
                
        PreparedStatement s = conn
            .prepareStatement(
                              "SELECT DISTINCT sid FROM annotatedLines as A");          
        ResultSet r = s.executeQuery();
                
        while(r.next()) {
            int sid = r.getInt(1);
            sids.add(sid);
        }
        r.close();
        s.close();
        return sids;
    }

    private static Map<Integer,String> getCodeFragments(Connection conn) throws SQLException, IOException {
                
        Collection<Integer> sids = getSids(conn);
        Map<Integer, String> sidToCode = new HashMap<Integer,String>();
                
        PreparedStatement s = conn
            .prepareStatement(
                              "SELECT line, L.sid, L.lid, A.uid "
                              + " FROM annotatedLines as A, lines as L "
                              + " WHERE A.sid=L.sid AND A.lid=L.lid AND A.uid like 'yam' "
                              + " AND L.sid=?");                
                
        for( Integer sid : sids ) {
                        
            System.out.println("sid " + sid);
                        
            String codeFragment = "";
            s.setInt(1, sid);
            ResultSet r = s.executeQuery();
                        
            if( r.next() ) {
                String line = r.getString(1);
                codeFragment = line;
            }
                        
            while(r.next()) {
                String line = r.getString(1);
                codeFragment += "\n" + line;
            }
            r.close();           
            sidToCode.put(sid, codeFragment);                   
        }
        s.close();
        return sidToCode;
    }
        
}
