package com.addi.application.config

import com.addi.application.stub.EmbeddedMockserverStub
import org.slf4j.LoggerFactory

data class ApplicationConfiguration(
    val embeddedMockServer: Boolean,
    val nationalRegistryUrl: String,
    val judicialArchiveUrl: String,
    val prospectQualifierUrl: String
) {
    companion object {
        private const val EMBEDDED_MOCKSERVER_ENV = "EMBEDDED_MOCKSERVER_STUB"
        private const val NATIONAL_REGISTRY_URL_ENV = "NATIONAL_REGISTRY_URL"
        private const val JUDICIAL_ARCHIVE_URL_ENV = "JUDICIAL_ARCHIVE_URL"
        private const val PROSPECT_QUALIFIER_URL_ENV = "PROSPECT_QUALIFIER_URL"

        private val log = LoggerFactory.getLogger(ApplicationConfiguration::class.java)

        /**
         * Load environment config.
         */
        fun load(): ApplicationConfiguration {
            val embeddedMockServer = (System.getenv(EMBEDDED_MOCKSERVER_ENV) ?: "true").toBoolean()
            return if (embeddedMockServer) {
                log.info("Environment variable $EMBEDDED_MOCKSERVER_ENV is enabled.")
                EmbeddedMockserverStub.start()
                Runtime.getRuntime().addShutdownHook(Thread {
                    EmbeddedMockserverStub.stop()
                })
                ApplicationConfiguration(
                    embeddedMockServer = embeddedMockServer,
                    nationalRegistryUrl = EmbeddedMockserverStub.getUrl(),
                    judicialArchiveUrl = EmbeddedMockserverStub.getUrl(),
                    prospectQualifierUrl = EmbeddedMockserverStub.getUrl(),
                )
            } else {
                ApplicationConfiguration(
                    embeddedMockServer = embeddedMockServer,
                    nationalRegistryUrl = System.getenv(NATIONAL_REGISTRY_URL_ENV)
                        ?: "http://localhost:8080/national-registry",
                    judicialArchiveUrl = System.getenv(JUDICIAL_ARCHIVE_URL_ENV)
                        ?: "http://localhost:8080/judicial-archive",
                    prospectQualifierUrl = System.getenv(PROSPECT_QUALIFIER_URL_ENV)
                        ?: "http://localhost:8080/prospect-qualifier",
                )
            }
        }
    }

}
