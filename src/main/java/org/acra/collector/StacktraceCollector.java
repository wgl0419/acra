package org.acra.collector;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.acra.ReportField;
import org.acra.builder.ReportBuilder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Set;

/**
 * Created by Lukas on 12.08.2016.
 */
public class StacktraceCollector extends Collector {
    public StacktraceCollector() {
        super(ReportField.STACK_TRACE, ReportField.STACK_TRACE_HASH);
    }

    @Override
    public boolean shouldCollect(Set<ReportField> crashReportFields, ReportField collect, ReportBuilder reportBuilder) {
        return collect == ReportField.STACK_TRACE || super.shouldCollect(crashReportFields, collect, reportBuilder);
    }

    @NonNull
    @Override
    public String collect(ReportField reportField, ReportBuilder reportBuilder) {
        switch (reportField) {
            case STACK_TRACE:
                return getStackTrace(reportBuilder.getMessage(), reportBuilder.getException());
            case STACK_TRACE_HASH:
                return getStackTraceHash(reportBuilder.getException());
            default:
                //will never happen
                throw new IllegalArgumentException();
        }
    }

    @NonNull
    private String getStackTrace(@Nullable String msg, @Nullable Throwable th) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);

        if (msg != null && !TextUtils.isEmpty(msg)) {
            printWriter.println(msg);
        }

        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        Throwable cause = th;
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        final String stacktraceAsString = result.toString();
        printWriter.close();

        return stacktraceAsString;
    }

    @NonNull
    private String getStackTraceHash(@Nullable Throwable th) {
        final StringBuilder res = new StringBuilder();
        Throwable cause = th;
        while (cause != null) {
            final StackTraceElement[] stackTraceElements = cause.getStackTrace();
            for (final StackTraceElement e : stackTraceElements) {
                res.append(e.getClassName());
                res.append(e.getMethodName());
            }
            cause = cause.getCause();
        }

        return Integer.toHexString(res.toString().hashCode());
    }
}
