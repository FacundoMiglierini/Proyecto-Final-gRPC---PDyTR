package com.unlp.pdtr.app;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import io.grpc.stub.StreamObserver;
import com.google.protobuf.Empty;
import com.unlp.pdtr.app.DatabaseServiceGrpc.DatabaseServiceImplBase;
import com.unlp.pdtr.app.DatabaseServiceOuterClass.DBRequest;

public class DatabaseServiceImpl extends DatabaseServiceImplBase
{
    private static Database database;
    private static ExecutorService executor; 

    public DatabaseServiceImpl() {
        database = new Database();
        executor = Executors.newFixedThreadPool(30);
    }

    @Override
    public StreamObserver<DBRequest> storeInDatabase(final StreamObserver<Empty> responseObserver) {
        return new StreamObserver<DBRequest>() {
            @Override
            public void onNext(DBRequest request) {
                Instant time = Instant.ofEpochSecond(request.getTime().getSeconds(), request.getTime().getNanos());
                executor.submit(() -> database.writeData(request.getLat(), request.getLong(), request.getDepartment(), request.getMeasure(), request.getValue(), time));
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("No se pudo guardar en la BD.");
                System.err.println(t.toString());
            }

            @Override
            public void onCompleted() {
                database.closeDatabase();
                responseObserver.onNext(Empty.getDefaultInstance());
                System.out.println("STORE IN DB OPERATION FINISHED");
            }
        };

    }
}
