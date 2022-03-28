/*
import java.sql.Connection
import java.sql.DriverManager


// the model class
data class Productos(val id: Int, val name: String)

fun main() {

    lateinit var connection: Connection


     //"jdbc:h2:mem:default"
    val jdbcUrl ="jdbc:h2:mem:default"

    // get the connection
    connection = DriverManager.getConnection(jdbcUrl, "", "")

    // prints true if the connection is valid
    println(connection.isValid(0))

    // the query is only prepared not executed
    val query = connection.prepareStatement("SELECT * FROM Productos")

    // the query is executed and results are fetched
    val result = query.executeQuery()

    // an empty list for holding the results
    val users = mutableListOf<Productos>()

    while (result.next()) {

        // getting the value of the id column
        val id = result.getInt("ProductID")

        // getting the value of the name column
        val name = result.getString("ProductName")

        /*
        constructing a User object and
        putting data into the list
         */
        users.add(Productos(id, name))
    }
    /*
    [User(id=1, name=Kohli), User(id=2, name=Rohit),
    User(id=3, name=Bumrah), User(id=4, name=Dhawan)]
     */
    println(users)
}

 */