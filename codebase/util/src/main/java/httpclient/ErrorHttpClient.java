package httpclient;

import java.text.MessageFormat;

interface Error {
   
}

public enum ErrorHttpClient implements Error {
  HTTP_PARSE_ERROR("http_parse_error", "parse error", FailureLeague.APPLICATION),
  HTTP_IO_ERROR("http_io_error", "failed or interrupted I/O operations", FailureLeague.APPLICATION),
  HTTP_KEY_MANANGEMENT_ERROR("http_io_error", "failed or interrupted I/O operations", FailureLeague.APPLICATION),
  HTTP_NO_SUCH_ALGORITHM_ERROR("http_io_error", "failed or interrupted I/O operations", FailureLeague.APPLICATION),
  HTTP_KEY_STORE_EXCEPTION_ERROR("http_io_error", "failed or interrupted I/O operations", FailureLeague.APPLICATION),
  HTTP_CERTIFICATE_ERROR("http_io_error", "failed or interrupted I/O operations", FailureLeague.APPLICATION),
  HTTP_PROTOCOL_ERROR("http_protocol_error", "An error in the HTTP protocol", FailureLeague.APPLICATION);
  
  private String errorCode;
  private String errorDescription;
  private FailureLeague failureLeague;

  ErrorHttpClient(String errorCode, String errorDescription, FailureLeague failureLeague) {
      this.errorCode = errorCode;
      this.errorDescription = errorDescription;
      this.failureLeague = failureLeague;
  }

  public String getErrorCode() {
      return errorCode;
  }

  public String getErrorDescription() {
      return errorDescription;
  }

  public FailureLeague getFailureLeague() {
      return failureLeague;
  }

  public String getFormattedErrorDescription(Object... args) {
      if (null == args)
          return errorDescription;
      return MessageFormat.format(errorDescription, args);
  }

  public String toString() {
      return errorCode + ": " + errorDescription;

  }

  public String toString(Object... args) {
      if (null == args)
          return errorCode + ": " + errorDescription;
      return errorCode + ": " + MessageFormat.format(errorDescription, args);
  }
}
