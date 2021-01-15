# tracecompass-rocm-ctf
Tracing pipeline that uses ROC-profiler and ROC-tracer as a source to display them in Trace Compass

## Build

### Requirements
- Java 11
- Maven

```
cd org.eclipse.tracecompass/
mvn clean install -Dmaven.test.skip=true -DskipTests
cd ../org.eclipse.tracecompass.incubator/
mvn clean install -Dmaven.test.skip=true -DskipTests
```

## Convert ROCm trace to CTF format

### Requirements
- Python 3
- Babeltrace 2
- Sqlite 3
 
```
python ctftrace.py <rocm_trace>.db
```

## Executing Trace Compass

The TraceCompass executable should be available at the following path:
`./tracecompass-incubator/rcp/org.eclipse.tracecompass.incubator.rcp.product/target/products/org.eclipse.tracecompass.incubator.rcp/linux/gtk/x86_64/trace-compass/`

To learn how to use Trace Compass, tutorials are available here: https://github.com/tuxology/tracevizlab
