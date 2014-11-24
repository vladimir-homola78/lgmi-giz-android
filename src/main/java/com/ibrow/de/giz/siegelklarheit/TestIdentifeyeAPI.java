package com.ibrow.de.giz.siegelklarheit;

/**
 * Test API using identifeye.ibrow.com on port 5001.
 * @author Pete
 */
class TestIdentifeyeAPI extends IdentifeyeAPI{


    protected final String testEndPoint="http://identifeye.ibrow.com:5001/";

    protected final String testImageBase="http://identifeye.ibrow.com:5001";

    @Override
    protected String getEndPoint(){
        return testEndPoint;
    }

    @Override
    protected String getImageBase(){
        return testImageBase;
    }

    public String getWebviewBaseURL(){
        return testEndPoint;
    }

}
