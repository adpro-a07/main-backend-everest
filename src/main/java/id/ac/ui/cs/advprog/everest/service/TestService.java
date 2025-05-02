package id.ac.ui.cs.advprog.everest.service;

import id.ac.ui.cs.advprog.kilimanjaro.hello.GreeterGrpc;
import id.ac.ui.cs.advprog.kilimanjaro.hello.HelloReply;
import id.ac.ui.cs.advprog.kilimanjaro.hello.HelloRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.stereotype.Service;

@Service
public class TestService {
    public void getHello() {

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext().build();

        GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(channel);

        HelloReply response = stub.sayHello(HelloRequest.newBuilder().setName("World").build());

        System.out.println(response);

        channel.shutdown();
    }
}
