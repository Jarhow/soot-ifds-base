package basic.common;

import soot.G;
import soot.Scene;
import soot.options.Options;

import java.io.File;
import java.util.Collections;

public class SootInit {
    public static void setSoot_inputClass(String inputClassPath) {
        G.reset();

        Options.v().set_src_prec(Options.src_prec_class);
        Options.v().set_process_dir(Collections.singletonList((inputClassPath)));
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_whole_program(true);
        Options.v().set_prepend_classpath(true);

        Options.v().set_output_format(Options.output_format_none);

        Options.v().set_drop_bodies_after_load(false);
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().setPhaseOption("cg", "all-reachable:true");
        //Options.v().setPhaseOption("cg.spark", "on");//SPARK生成的call graph更准确
        Scene.v().setSootClassPath(Scene.v().getSootClassPath());

        Scene.v().loadNecessaryClasses();
        System.out.println(Scene.v().getApplicationClasses().size());
    }

}
