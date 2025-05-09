package id.ac.ui.cs.advprog.everest.config;

import id.ac.ui.cs.advprog.everest.common.service.AuthServiceGrpcClient;
import id.ac.ui.cs.advprog.everest.common.service.UserServiceGrpcClient;
import id.ac.ui.cs.advprog.everest.common.utils.RequestMetadataUtil;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.AuthServiceGrpc;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcConfig {
    @Value("${auth.grpc.secure:false}")
    private boolean useSecure;

    private ManagedChannel managedChannel;

    @Bean
    public ManagedChannel authServiceChannel(
            @Value("${auth.grpc.host}") String host,
            @Value("${auth.grpc.port}") int port
    ) {
        this.managedChannel = useSecure
                ? ManagedChannelBuilder.forAddress(host, port).useTransportSecurity().build()
                : ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        return this.managedChannel;
    }

    // --- AuthServiceGrpcClient ---
    @Bean
    public AuthServiceGrpc.AuthServiceBlockingStub authServiceBlockingStub(ManagedChannel channel) {
        return AuthServiceGrpc.newBlockingStub(channel);
    }

    @Bean
    public AuthServiceGrpcClient authServiceClient(
            AuthServiceGrpc.AuthServiceBlockingStub stub,
            RequestMetadataUtil requestMetadataUtil
    ) {
        return new AuthServiceGrpcClient(stub, requestMetadataUtil);
    }

    // --- UserServiceGrpcClient ---
    @Bean
    public UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub(ManagedChannel channel) {
        return UserServiceGrpc.newBlockingStub(channel);
    }

    @Bean
    public UserServiceGrpcClient userServiceClient(
            UserServiceGrpc.UserServiceBlockingStub stub,
            RequestMetadataUtil requestMetadataUtil
    ) {
        return new UserServiceGrpcClient(stub, requestMetadataUtil);
    }

    @PreDestroy
    public void shutdownChannel() {
        if (this.managedChannel != null && !this.managedChannel.isShutdown()) {
            this.managedChannel.shutdown();
        }
    }
}
