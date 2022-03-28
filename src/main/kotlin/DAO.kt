import java.sql.Connection
import java.sql.Date
import java.sql.DriverManager
import java.sql.SQLException
import java.text.SimpleDateFormat


fun main() {

    val c = ConnectionBuilder()
    println("conectando.....")

    if (c.connection.isValid(10)) {
        println("Conexión válida")

        c.connection.use {
            val h2DAO = BookDAO(c.connection)

            // Creamos la tabla o la vaciamos si ya existe
            h2DAO.prepareTable()

            // Insertamos 4 usuarios
            var id = -1
            val format = SimpleDateFormat("MM/dd/yyyy")
            val myDate = format.parse("10/10/2009")
            repeat(4)
            {
                h2DAO.insertBook(MyBook(  id ++ ,author = "Gambardella, Matthew", title = "XML Developer's Guide", genre = "Computer", price = 44.95F, publish_date = myDate, description = "An in-depth look at creating applications with XML."  ))
            }  // Buscar un usuario
            var u = h2DAO.selectBook(1)

            // Actualizar un usuario
            if (u != null) {
                u.description = "Nuevo libro"
                h2DAO.updateBook(u)
            }
            // Borrar un usuario
            h2DAO.deleteUser(2)

            // Seleccionar todos los usuarios
            println(h2DAO.selectAllBook())
        }
    } else
        println("Conexión ERROR")
}

/**
 * AbstractDAO.java This DAO class provides CRUD database operations for the
 * table users in the database.
 *
 * @author edu
 */

class ConnectionBuilder {
    // TODO Auto-generated catch block
    lateinit var connection: Connection
    private val jdbcURL = "jdbc:h2:mem:default"
    private val jdbcUsername = ""
    private val jdbcPassword = ""

    init {
        try {
            connection = DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword)
        } catch (e: SQLException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

}


class BookDAO(private val c: Connection) {

    companion object {
        private const val SCHEMA = "default"
        private const val TABLE = "CATALOG"
        private const val TRUNCATE_TABLE_CATALOG_SQL = "TRUNCATE TABLE CATALOGO"
        private const val CREATE_TABLE_CATALOG_SQL =
            "CREATE TABLE CATALOG (id  number(3) NOT NULL AUTO_INCREMENT,author varchar(120) NOT NULL,title varchar(220) NOT NULL,genre varchar(20),price decimal (10),publish_date date ,description varchar(120),PRIMARY KEY (id))"
        private const val INSERT_BOOK_SQL =
            "INSERT INTO CATALOG" + "  (author, title, genre, price, publish_date,description ) VALUES " + " (?, ?, ?, ?, ? ,?);"
        private const val SELECT_CATALOG_BY_ID =
            "select  id, author, title, genre, price, publish_date, description from CATALOG where id =?"
        private const val SELECT_ALL_BOOKS = "select * from CATALOG"
        private const val DELETE_BOOKS_SQL = "delete from CATALOG where id = ?;"
        private const val UPDATE_BOOKS_SQL =
            "update CATALOG set author = ?,title = ?, genre =?, price = ?, publish_date = ?, description = ? where id = ?;"
    }


    fun prepareTable() {
        val metaData = c.metaData
        val rs = metaData.getTables(null, SCHEMA, TABLE, null)

        if (!rs.next()) createTable() else truncateTable()
    }

    private fun truncateTable() {
        println(TRUNCATE_TABLE_CATALOG_SQL)
        // try-with-resource statement will auto close the connection.
        try {
            c.createStatement().use { st ->
                st.execute(TRUNCATE_TABLE_CATALOG_SQL)
            }
            //Commit the change to the database
            c.commit()
        } catch (e: SQLException) {
            printSQLException(e)
        }
    }

    private fun createTable() {
        println(CREATE_TABLE_CATALOG_SQL)
        // try-with-resource statement will auto close the connection.
        try {
            //Get and instance of statement from the connection and use
            //the execute() method to execute the sql
            c.createStatement().use { st ->
                //SQL statement to create a table
                st.execute(CREATE_TABLE_CATALOG_SQL)
            }
            //Commit the change to the database
            c.commit()
        } catch (e: SQLException) {
            printSQLException(e)
        }
    }

    fun insertBook(book: MyBook) {
        println(INSERT_BOOK_SQL)
        // try-with-resource statement will auto close the connection.

        val sqldate = java.sql.Date(book.publish_date.time)
        try {
            c.prepareStatement(INSERT_BOOK_SQL).use { st ->
                st.setString(1, book.author)
                st.setString(2, book.title)
                st.setString(3, book.genre)
                st.setFloat(4, book.price)
                st.setDate(5, sqldate)
                st.setString(6, book.description)
                println(st)
                st.executeUpdate()

            }
            //Commit the change to the database
            c.commit()
        } catch (e: SQLException) {
            printSQLException(e)
        }
    }

    fun selectBook(id: Int): MyBook? {
        var book: MyBook? = null
        // Step 1: Establishing a Connection
        try {
            c.prepareStatement(SELECT_CATALOG_BY_ID).use { st ->
                st.setInt(1, id)
                println(st)
                // Step 3: Execute the query or update query
                val rs = st.executeQuery()

                // Step 4: Process the ResultSet object.
                while (rs.next()) {
                    val author = rs.getString("author")
                    val title = rs.getString("title")
                    val genre = rs.getString("genre")
                    val price = rs.getFloat("price")
                    val publish_date = rs.getDate("publish_date")
                    val description = rs.getString("description")

                    book = MyBook(id, author, title, genre, price, publish_date, description)
                    //author, title, genre, price, publish_date, description
                }
            }

        } catch (e: SQLException) {
            printSQLException(e)
        }
        return book
    }

    fun selectAllBook(): List<MyBook> {

        // using try-with-resources to avoid closing resources (boiler plate code)
        var book: MutableList<MyBook> = ArrayList()
        // Step 1: Establishing a Connection
        try {
            c.prepareStatement(SELECT_ALL_BOOKS).use { st ->
                println(st)
                // Step 3: Execute the query or update query
                val rs = st.executeQuery()

                // Step 4: Process the ResultSet object.
                while (rs.next()) {
                    val id = rs.getInt("id")
                    val author = rs.getString("author")
                    val title = rs.getString("title")
                    val genre = rs.getString("genre")
                    val price = rs.getFloat("price")
                    val publish_date = rs.getDate("publish_date")
                    val description = rs.getString("description")

                    book.add( MyBook(id, author, title, genre, price, publish_date, description))
                }
            }

        } catch (e: SQLException) {
            printSQLException(e)
        }
        return book
    }

    fun deleteUser(id: Int): Boolean {
        var rowDeleted = false

        try {
            c.prepareStatement(DELETE_BOOKS_SQL).use { st ->
                st.setInt(1, id)
                rowDeleted = st.executeUpdate() > 0
            }
            //Commit the change to the database
            c.commit()
        } catch (e: SQLException) {
            printSQLException(e)
        }
        return rowDeleted
    }

    fun updateBook(book: MyBook): Boolean {
        var rowUpdated = false
        val sqldate = java.sql.Date(book.publish_date.time)
        try {
            c.prepareStatement(UPDATE_BOOKS_SQL).use { st ->
                st.setString(1, book.author)
                st.setString(2, book.title)
                st.setString(3, book.genre)
                st.setFloat(4, book.price)
                st.setDate(5, sqldate)
                st.setString(6, book.description)
                st.setInt(7,book.id)
                rowUpdated = st.executeUpdate() > 0
            }
            //Commit the change to the database
            c.commit()
        } catch (e: SQLException) {
            printSQLException(e)
        }
        return rowUpdated
    }

    private fun printSQLException(ex: SQLException) {
        for (e in ex) {
            if (e is SQLException) {
                e.printStackTrace(System.err)
                System.err.println("SQLState: " + e.sqlState)
                System.err.println("Error Code: " + e.errorCode)
                System.err.println("Message: " + e.message)
                var t = ex.cause
                while (t != null) {
                    println("Cause: $t")
                    t = t.cause
                }
            }
        }
    }


}
