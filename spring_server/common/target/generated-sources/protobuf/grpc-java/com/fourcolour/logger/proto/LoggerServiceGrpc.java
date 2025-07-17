package com.fourcolour.logger.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.60.1)",
    comments = "Source: logger.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class LoggerServiceGrpc {

  private LoggerServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "proto.LoggerService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.fourcolour.logger.proto.LoggerProto.LogRequest,
      com.fourcolour.logger.proto.LoggerProto.LogResponse> getLogEventMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "LogEvent",
      requestType = com.fourcolour.logger.proto.LoggerProto.LogRequest.class,
      responseType = com.fourcolour.logger.proto.LoggerProto.LogResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.fourcolour.logger.proto.LoggerProto.LogRequest,
      com.fourcolour.logger.proto.LoggerProto.LogResponse> getLogEventMethod() {
    io.grpc.MethodDescriptor<com.fourcolour.logger.proto.LoggerProto.LogRequest, com.fourcolour.logger.proto.LoggerProto.LogResponse> getLogEventMethod;
    if ((getLogEventMethod = LoggerServiceGrpc.getLogEventMethod) == null) {
      synchronized (LoggerServiceGrpc.class) {
        if ((getLogEventMethod = LoggerServiceGrpc.getLogEventMethod) == null) {
          LoggerServiceGrpc.getLogEventMethod = getLogEventMethod =
              io.grpc.MethodDescriptor.<com.fourcolour.logger.proto.LoggerProto.LogRequest, com.fourcolour.logger.proto.LoggerProto.LogResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "LogEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.fourcolour.logger.proto.LoggerProto.LogRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.fourcolour.logger.proto.LoggerProto.LogResponse.getDefaultInstance()))
              .setSchemaDescriptor(new LoggerServiceMethodDescriptorSupplier("LogEvent"))
              .build();
        }
      }
    }
    return getLogEventMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static LoggerServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<LoggerServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<LoggerServiceStub>() {
        @java.lang.Override
        public LoggerServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new LoggerServiceStub(channel, callOptions);
        }
      };
    return LoggerServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static LoggerServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<LoggerServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<LoggerServiceBlockingStub>() {
        @java.lang.Override
        public LoggerServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new LoggerServiceBlockingStub(channel, callOptions);
        }
      };
    return LoggerServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static LoggerServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<LoggerServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<LoggerServiceFutureStub>() {
        @java.lang.Override
        public LoggerServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new LoggerServiceFutureStub(channel, callOptions);
        }
      };
    return LoggerServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void logEvent(com.fourcolour.logger.proto.LoggerProto.LogRequest request,
        io.grpc.stub.StreamObserver<com.fourcolour.logger.proto.LoggerProto.LogResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getLogEventMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service LoggerService.
   */
  public static abstract class LoggerServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return LoggerServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service LoggerService.
   */
  public static final class LoggerServiceStub
      extends io.grpc.stub.AbstractAsyncStub<LoggerServiceStub> {
    private LoggerServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LoggerServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new LoggerServiceStub(channel, callOptions);
    }

    /**
     */
    public void logEvent(com.fourcolour.logger.proto.LoggerProto.LogRequest request,
        io.grpc.stub.StreamObserver<com.fourcolour.logger.proto.LoggerProto.LogResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getLogEventMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service LoggerService.
   */
  public static final class LoggerServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<LoggerServiceBlockingStub> {
    private LoggerServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LoggerServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new LoggerServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.fourcolour.logger.proto.LoggerProto.LogResponse logEvent(com.fourcolour.logger.proto.LoggerProto.LogRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getLogEventMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service LoggerService.
   */
  public static final class LoggerServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<LoggerServiceFutureStub> {
    private LoggerServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LoggerServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new LoggerServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.fourcolour.logger.proto.LoggerProto.LogResponse> logEvent(
        com.fourcolour.logger.proto.LoggerProto.LogRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getLogEventMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_LOG_EVENT = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_LOG_EVENT:
          serviceImpl.logEvent((com.fourcolour.logger.proto.LoggerProto.LogRequest) request,
              (io.grpc.stub.StreamObserver<com.fourcolour.logger.proto.LoggerProto.LogResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getLogEventMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.fourcolour.logger.proto.LoggerProto.LogRequest,
              com.fourcolour.logger.proto.LoggerProto.LogResponse>(
                service, METHODID_LOG_EVENT)))
        .build();
  }

  private static abstract class LoggerServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    LoggerServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.fourcolour.logger.proto.LoggerProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("LoggerService");
    }
  }

  private static final class LoggerServiceFileDescriptorSupplier
      extends LoggerServiceBaseDescriptorSupplier {
    LoggerServiceFileDescriptorSupplier() {}
  }

  private static final class LoggerServiceMethodDescriptorSupplier
      extends LoggerServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    LoggerServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (LoggerServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new LoggerServiceFileDescriptorSupplier())
              .addMethod(getLogEventMethod())
              .build();
        }
      }
    }
    return result;
  }
}
