/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knu.univ.lingvo.matrixfactorization;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author taras
 */
public class DB {

    String url = "jdbc:postgresql://195.68.210.43/lingvo";
    String user = "taras";
    String password = "tarastaras";
    Connection con = null;

    private DB() {
        try {
            con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT VERSION()");

            if (rs.next()) {
                System.out.println(rs.getString(1));
            }

        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(DB.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);

        }
    }

    public int saveWord(String word) throws SQLException {
        PreparedStatement st = con.prepareStatement("SELECT index FROM words where word = ?");
        st.setString(1, word);
        ResultSet rs = st.executeQuery();
        if (rs.next()) { //exists
            return rs.getInt("index");
        }

        st = con.prepareStatement("INSERT INTO words (word) VALUES(?)");
        st.setString(1, word);
        st.executeUpdate();
        return saveWord(word);
    }
    
    public void saveArticle(String word) throws SQLException {
        PreparedStatement st = con.prepareStatement("SELECT title FROM articles where title = ?");
        st.setString(1, word);
        ResultSet rs = st.executeQuery();
        if (rs.next()) { //exists
            return;
        }

        st = con.prepareStatement("INSERT INTO articles (title) VALUES(?)");
        st.setString(1, word);
        st.executeUpdate();
    }

    public void saveVector(String[] columns, String[] values, int weight) throws SQLException {
        assert (columns.length == values.length);

        int valNo[] = new int[values.length];
        for (int i = 0; i < valNo.length; i++) {
            if (values[i] != null)
                valNo[i] = saveWord(values[i]);
            else
                valNo[i] = -1;
        }

        StringBuffer query = new StringBuffer();
        query.append("select * from tensor where ");
        for (int i = 0; i < values.length - 1; i++) {
            String val = "" + valNo[i];
            String col = columns[i];
            query.append(col + "=" + val + " and ");
        }
        query.append(columns[columns.length - 1] + "=" + valNo[valNo.length - 1] + ";");

        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(query.toString());

        if (rs.next()) {
            int oldWeight = rs.getInt("count");
            int newWeight = oldWeight + weight;
            query = new StringBuffer();
            query.append("UPDATE tensor SET count = " + newWeight);
            query.append(" where ");
            for (int i = 0; i < values.length - 1; i++) {
                String val = "" + valNo[i];
                String col = columns[i];
                query.append(col + "=" + val + " and ");
            }
            query.append(columns[columns.length - 1] + "=" + valNo[valNo.length - 1] + ";");

            st = con.createStatement();
            st.execute(query.toString());
        } else {
            query = new StringBuffer();
            query.append("INSERT INTO tensor ( ");
            for (int i = 0; i < columns.length; i++) {
                query.append(columns[i] + ", ");
            }
            query.append("count) values (");            
            for (int i = 0; i < columns.length; i++) {
                query.append(valNo[i] + ", ");
            }
            
            query.append(weight + ");");

            st = con.createStatement();
            st.execute(query.toString());
        }

    }
    private static DB instance = new DB();

    public static DB getInstance() {
        return instance;
    }

    public static void main(String[] args) throws Exception {
        //System.out.println("" + getInstance().saveWord("first word"));
        //System.out.println("" + getInstance().saveWord("first wor"));
        //System.out.println("" + getInstance().saveWord("first word"));
        getInstance().saveArticle("first article");
    }
}
