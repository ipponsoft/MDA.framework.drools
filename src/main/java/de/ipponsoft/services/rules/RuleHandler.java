package de.ipponsoft.services.rules;

import javax.rules.RuleServiceProvider;
import javax.rules.RuleServiceProviderManager;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.DecisionTableConfiguration;
import org.drools.builder.DecisionTableInputType;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.command.CommandFactory;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatelessKnowledgeSession;

/**
 * @author chm
 * Kapsel zum Handling von Rule Engines gemŠ§ JSR94
 */
public class RuleHandler {
	
	final public static int TYPE_TABLE = 1;
	final public static int TYPE_CODE = 2;
	final public static int TYPE_WORKFLOW = 3;

	protected static RuleServiceProvider serviceProvider = null;
	protected KnowledgeBase kbase = null;

	/**
	 * Static initializer
	 */
	static {
		try {
			Class.forName("org.drools.jsr94.rules.RuleServiceProviderImpl");
			serviceProvider = RuleServiceProviderManager.getRuleServiceProvider("http://drools.org/");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	public RuleHandler(String ruleFile, int ruleType) throws Exception {
		kbase = readKnowledgeBase(ruleFile, ruleType);
	}
	
	public Result fireOn(Object baseObj) throws Exception {
		Result ret = new Result();
		StatelessKnowledgeSession ksession = kbase.newStatelessKnowledgeSession();
		ksession.setGlobal("RET", ret);
		ksession.execute(CommandFactory.newInsert(baseObj, "RET"));
		return ret;
	}
		
	private static KnowledgeBase readKnowledgeBase(String ruleFile, int ruleType) throws Exception {
		KnowledgeBuilder kbuilder = null;
		
		switch (ruleType) {
		case TYPE_TABLE: kbuilder = createKnowledgeBuilderXLS(ruleFile); break;
		case TYPE_CODE: kbuilder = createKnowledgeBuilderDRL(ruleFile); break;
		case TYPE_WORKFLOW: kbuilder = createKnowledgeBuilderDRF(ruleFile); break;
		}

		KnowledgeBuilderErrors errors = kbuilder.getErrors();
		if (errors.size() > 0) {
			for (KnowledgeBuilderError error: errors) {
				System.err.println(error);
			}
			throw new IllegalArgumentException("Could not parse knowledge.");
		}
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		return kbase;
	}

	private static KnowledgeBuilder createKnowledgeBuilder(String ruleFile, ResourceType type) throws Exception {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		kbuilder.add(ResourceFactory.newClassPathResource(ruleFile), type);
		return kbuilder;
	}

	private static KnowledgeBuilder createKnowledgeBuilderDRL(String ruleFile) throws Exception {
		return createKnowledgeBuilder(ruleFile, ResourceType.DRL);
	}

	private static KnowledgeBuilder createKnowledgeBuilderDRF(String ruleFile) throws Exception {
		return createKnowledgeBuilder(ruleFile, ResourceType.DRF);
	}

	private static KnowledgeBuilder createKnowledgeBuilderXLS(String ruleFile) throws Exception {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();	
		DecisionTableConfiguration config = KnowledgeBuilderFactory.newDecisionTableConfiguration();
		config.setInputType(DecisionTableInputType.XLS);
		kbuilder.add(ResourceFactory.newClassPathResource(ruleFile), ResourceType.DTABLE, config);
		return kbuilder;
	}

}
