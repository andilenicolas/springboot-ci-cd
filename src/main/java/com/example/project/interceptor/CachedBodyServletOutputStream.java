package com.example.project.interceptor;

import java.io.IOException;
import jakarta.servlet.WriteListener;
import java.io.ByteArrayOutputStream;
import jakarta.servlet.ServletOutputStream;

public class CachedBodyServletOutputStream extends ServletOutputStream 
{
    private ByteArrayOutputStream cachedContent;

    public CachedBodyServletOutputStream() {
        this.cachedContent = new ByteArrayOutputStream();
    }

    @Override
    public void write(int b) throws IOException {
        cachedContent.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        cachedContent.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        cachedContent.write(b, off, len);
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        throw new UnsupportedOperationException();
    }

    public byte[] getCachedContent() {
        return cachedContent.toByteArray();
    }
}