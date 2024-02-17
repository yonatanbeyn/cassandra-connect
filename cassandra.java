import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import java.util.UUID;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        cassandraConnect();


    }

    public  static void cassandraConnect(){
        String statement=""" 
                             INSERT INTO keyspaceNameTest.BOOK_TABLE(id, title, author,publisher, subject)
                              VALUES (db048caa-87a8-43a2-a571-068dd8ff698e, 'java cassandra', 
                              'yonatan', 'pact', 'about cassandra and how to use with java ');
                          """;
        Cluster cluster = null;
        try {
            cluster = Cluster.builder()                                                    // (1)
                    .addContactPoint("127.0.0.1")
                    .withClusterName("datacenter2")
                    .build();
            Session session = cluster.connect();
            /*
            StringBuilder sb =
                    new StringBuilder("CREATE KEYSPACE IF NOT EXISTS ")
                            .append("keyspaceNameTest").append(" WITH replication = {")
                            .append("'class':'SimpleStrategy")
                            .append("','replication_factor':").append(1)
                            .append("};");
            ResultSet rs = session.execute(sb.toString());


            StringBuilder book = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                    .append("keyspaceNameTest.BOOK_TABLE").append("(")
                    .append("id uuid PRIMARY KEY, ")
                    .append("title text,")
                    .append("author text,")
                    .append("publisher text,")
                    .append("subject text);");

            String query = book.toString();
            session.execute(query);

             */

                Book book1 = Book.builder()
                        .title("java cassandra")
                        .publisher("pact")
                        .author("yonatan")
                        .id(UUID.fromString("db048caa-87a8-43a2-a571-068dd8ff698e"))
                        .subject("about cassandra and how to use with java ")
                        .build();
                StringBuilder sb1 = new StringBuilder("INSERT INTO ").append("keyspaceNameTest.BOOK_TABLE")
                        .append("(id, title, author,publisher, subject) ").append("VALUES (")
                        .append(book1.getId()).append(", '")
                        .append(book1.getTitle())
                        .append("', '").
                        append(book1.getAuthor())
                        .append("', '")
                        .append(book1.getPublisher())
                        .append("', '")
                        .append(book1.getSubject())
                        .append("');");
                final String query1 = sb1.toString();
                session.execute(query1);




            // System.out.println(row.getString("release_version"));                          
        } finally {
            if (cluster != null) cluster.close();                                         
        };
    }



}
