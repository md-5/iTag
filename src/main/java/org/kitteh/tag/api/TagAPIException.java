package org.kitteh.tag.api;

public class TagAPIException extends RuntimeException
{

    private static final long serialVersionUID = 1L;

    public TagAPIException(String message)
    {
        super( message );
    }

    public TagAPIException(String message, Throwable cause)
    {
        super( message, cause );
    }
}
