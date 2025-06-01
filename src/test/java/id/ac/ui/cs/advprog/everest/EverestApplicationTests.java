package id.ac.ui.cs.advprog.everest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.public-key=LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUlJQ0lqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FnOEFNSUlDQ2dLQ0FnRUFtdUdDZGN3Q0VJLzJwYWtWeUFTeQpOYVhhM3p5SUtIT1p1SFQxZXk5UjVvWk5SU2dkNlpvRmJtSVhLWlRGZC8xbldOQVkxZVVyTXhVZlc2VTNTY2NyCnAvM3NYUjRRaTBwUWF0ZHBhWXJyQ0pGdk1qRmVPSWpGMU9Tcm43UjQ1ZC94NjZXd2pKUlRJeXRFUjIzUHhsOTEKd0gvc1AvNWdBWnZkOXZlZlhqa2lsNWVYV2FkdHRqMmYxeUgzK1NtYithNTBYMVZ0VzVoZzV0c2xGRThBM1NBaAp5c096WGRXZndrYzd4VlI1KzhkaDRGS2xRU0d6d3J3WHRTMFVHUUIrYXNDd2RHdE1sRnJraXNMYytLZmdsN1g3CjdwWEZkYW1uRStjclNIN2c0UERCVVFJMkorSzAyQkQ2dTE5OTZCWVhBRDRmUXBZK1VtaTFJaW5UWThhNXpOT1cKUmRDcFE0RDVsblE1Wm1WZ2FiZkJMa2MrMENRSGE5dXpRUXplNGdMak4vdjl4akhxc0JKVlgxenFxRFQwQ1cyRgpmekF6NnliM2JPRzM4OVpVZ2lBaDR3QnIrTGpxeTlmcGw5dTU3ZWdKcmdWT1d1T1Juc1FJbDJFZjBRNDdQaDhDCk1iQ1o1ejBYbUNrbFkwYWMzL0JDb2JJdTVwNTlGdGhvb0NpcVJUYUx1QlZ2Z0hwKzVUeTcvL0tVUGM2LzRtaEYKOGo1eXkzbUllZnZuQVNHQ1F6ZHYvbFVpMGpwNmtMODVSeXcxMmUyN2txZ1J1NmdsZ09ycjlqbmxENmNHZFIzUQpySzNwUm9TcHJETlUvMDBERnNkODFsQ0o0WU1JN0tzV1ViSldGcnI2b1k3QkErYzY0K0ZNMGtJcVRWR29KN1o0Ck5KRUFvQkk5ZU1ENXk2b0llc3RzNHFrQ0F3RUFBUT09Ci0tLS0tRU5EIFBVQkxJQyBLRVktLS0tLQ=="
})
class EverestApplicationTests {

    @Test
    void contextLoads() {
    }
}
