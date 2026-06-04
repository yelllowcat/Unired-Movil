const errorHandler = (err, req, res, next) => {
  let { statusCode, message } = err;

  if (!err.isOperational) {
    console.error('Unexpected error:', err);
    statusCode = 500;
    message = 'Internal Server Error';
  }

  res.locals.errorMessage = err.message;

  const response = {
    success: false,
    message,
    ...(process.env.NODE_ENV === 'development' && { stack: err.stack }),
  };

  res.status(statusCode || 500).json(response);
};

export default errorHandler;
