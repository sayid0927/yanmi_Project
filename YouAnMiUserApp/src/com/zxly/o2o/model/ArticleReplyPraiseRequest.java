package com.zxly.o2o.model;

import com.zxly.o2o.request.BaseRequest;

/**
 *     @author huangbin  @version 创建时间：2015-1-27 下午4:55:30    类说明: 
 */
public class ArticleReplyPraiseRequest extends BaseRequest {

	public ArticleReplyPraiseRequest(long id) {
		addParams("id", id);
	}

	@Override
	protected String method() {
		return "article/reply/praise";
	}

}
