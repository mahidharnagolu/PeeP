import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { useAuthStore } from '@/stores/authStore';
import { useRouter } from 'expo-router';
import { Settings, LogOut } from 'lucide-react-native';

export default function ProfileScreen() {
    const { profile, signOut } = useAuthStore();
    const router = useRouter();

    const handleSignOut = async () => {
        await signOut();
        router.replace('/auth/login');
    };

    return (
        <View style={styles.container}>
            {/* BeReal-style centered header */}
            <View style={styles.header}>
                <Text style={styles.logo}>PeeP.</Text>
            </View>

            <View style={styles.content}>
                {/* Avatar */}
                <View style={styles.avatarContainer}>
                    <View style={styles.avatar}>
                        <Text style={styles.avatarText}>
                            {profile?.username?.charAt(0).toUpperCase() || 'P'}
                        </Text>
                    </View>
                    <Text style={styles.username}>
                        {profile?.username || 'user'}.
                    </Text>
                    <Text style={styles.handle}>@{profile?.username || 'user'}</Text>
                </View>

                {/* Actions */}
                <View style={styles.actions}>
                    <TouchableOpacity style={styles.actionButton} activeOpacity={0.7}>
                        <Text style={styles.actionButtonText}>Share Profile</Text>
                    </TouchableOpacity>

                    <TouchableOpacity
                        style={styles.signOutButton}
                        onPress={handleSignOut}
                        activeOpacity={0.7}
                    >
                        <LogOut color="#FF3B30" size={18} />
                        <Text style={styles.signOutText}>Sign Out</Text>
                    </TouchableOpacity>
                </View>
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#000000',
    },
    header: {
        paddingTop: 56,
        paddingBottom: 16,
        alignItems: 'center',
    },
    logo: {
        color: '#FFFFFF',
        fontSize: 26,
        fontWeight: '800',
        letterSpacing: 0.5,
    },
    content: {
        flex: 1,
        paddingHorizontal: 20,
    },
    avatarContainer: {
        alignItems: 'center',
        marginTop: 40,
        marginBottom: 40,
    },
    avatar: {
        width: 100,
        height: 100,
        borderRadius: 50,
        backgroundColor: '#1A1A1A',
        justifyContent: 'center',
        alignItems: 'center',
        marginBottom: 16,
    },
    avatarText: {
        fontSize: 40,
        fontWeight: '700',
        color: '#FFFFFF',
    },
    username: {
        fontSize: 28,
        fontWeight: '800',
        color: '#FFFFFF',
        marginBottom: 4,
    },
    handle: {
        fontSize: 15,
        color: '#999999',
    },
    actions: {
        gap: 12,
        marginTop: 20,
    },
    actionButton: {
        backgroundColor: '#FFFFFF',
        paddingVertical: 14,
        borderRadius: 12,
        alignItems: 'center',
    },
    actionButtonText: {
        color: '#000000',
        fontSize: 16,
        fontWeight: '700',
    },
    signOutButton: {
        flexDirection: 'row',
        backgroundColor: '#1A1A1A',
        paddingVertical: 14,
        borderRadius: 12,
        alignItems: 'center',
        justifyContent: 'center',
        gap: 8,
    },
    signOutText: {
        color: '#FF3B30',
        fontSize: 16,
        fontWeight: '600',
    },
});
