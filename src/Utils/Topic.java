package Utils;

public class Topic {
    // all TREC-6 topics
    public static int[] topics_all = {301, 302, 303, 304, 305, 306, 307, 308, 309, 310,
            311, 312, 313, 314, 315, 316, 317, 318, 319, 320,
            321, 322, 323, 324, 325, 326, 327, 328, 329, 330,
            331, 332, 333, 334, 335, 336, 337, 338, 339, 340,
            341, 342, 343, 344, 345, 346, 347, 348, 349, 350};

    // topics that above 100 relevant documents
    public static int[] topics_100 = {301, 304, 306, 307, 311,
            313, 318, 319, 321, 324,
            331, 332, 343, 346, 347};

    // topics that above 50 relevant documents
    public static int[] topics_50 = {301, 302, 304, 306, 307, 311, 313, 315, 318, 319,
            321, 323, 324, 330, 331, 332, 333, 335, 337, 340,
            341, 343, 346, 347, 349, 350};

    // topics that above 30 relevant documents
    public static int[] topics_30 = {301, 302, 304, 305, 306, 307, 311, 313, 314, 315,
            316, 318, 319, 321, 322, 323, 324, 326, 329, 330,
            331, 332, 333, 335, 337, 340, 341, 343, 345, 346,
            347, 349, 350};

    // Liu
    public static int[] topics_Liu = {301, 304, 306, 307, 311,
            321, 324, 329, 330, 331,
            332, 335, 341, 343, 347};
}
