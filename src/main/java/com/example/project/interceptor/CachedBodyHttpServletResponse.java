package com.example.project.interceptor;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

public class CachedBodyHttpServletResponse extends HttpServletResponseWrapper 
{
    private CachedBodyServletOutputStream cachedBodyOutputStream;
    private PrintWriter writer;
    private String characterEncoding;

    public CachedBodyHttpServletResponse(HttpServletResponse response) throws IOException {
        super(response);
        this.characterEncoding = response.getCharacterEncoding();
        this.cachedBodyOutputStream = new CachedBodyServletOutputStream();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (writer != null) {
            throw new IllegalStateException("getWriter() has already been called on this response.");
        }
        return cachedBodyOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new PrintWriter(new OutputStreamWriter(cachedBodyOutputStream, getCharacterEncoding()));
        }
        return writer;
    }

    public String getCachedContent() throws IOException {
        if (writer != null) {
            writer.flush();
        }
        byte[] bytes = cachedBodyOutputStream.getCachedContent();
        return new String(bytes, characterEncoding);
    }

    public byte[] getCachedContentBytes() throws IOException {
        if (writer != null) {
            writer.flush();
        }
        return cachedBodyOutputStream.getCachedContent();
    }

    public void copyBodyToResponse() throws IOException {
        if (writer != null) {
            writer.flush();
        }
        byte[] body = cachedBodyOutputStream.getCachedContent();
        if (body.length > 0) {
            getResponse().getOutputStream().write(body);
        }
    }
}